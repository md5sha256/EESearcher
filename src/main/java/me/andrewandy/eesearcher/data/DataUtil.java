package me.andrewandy.eesearcher.data;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.zaxxer.hikari.pool.HikariPool;
import me.andrewandy.eesearcher.ExamSession;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.jetbrains.annotations.NotNull;

import javax.sql.rowset.serial.SerialBlob;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

public class DataUtil {

    // %1
    public static final String TABLE_NAME = "EEData";
    // %2
    public static final String COLUMN_TITLE = "title";
    // %3
    public static final String COLUMN_SUBJECT = "subject";
    // %4
    public static final String COLUMN_EXAM_YEAR = "exam_year";
    // %5
    public static final String COLUMN_RESEARCH_QUESTION = "research_question";
    // %6
    public static final String COLUMN_PDF = "compressed_pdf";

    @Inject
    @Named("internal-pool")
    private HikariPool pool;
    @Inject
    private Parser parser;
    @Inject
    private SubjectDatabase subjectDatabase;
    @Inject
    private ScheduledExecutorService executorService;

    private static String generateSqlConstraints(@NotNull QueryParameters parameters, int maxQueries) {

        final String pattern = parameters.regex;
        final char[] flags = parameters.flags;
        final Set<Subject> subjects = parameters.subjects;
        final ExamSessionConstraint sessionConstraint = parameters.examSessionConstraint;
        final String[] rawSubjects;

        if (subjects == null) {
            rawSubjects = new String[0];
        } else {
            rawSubjects = subjects.stream()
                    .map(Subject::getDisplayName)
                    .map(String::toLowerCase)
                    .distinct()
                    .toArray(String[]::new);
        }
        if (maxQueries == 0 || maxQueries < -1) {
            throw new IllegalArgumentException(String.format("Invalid MaxQueries: %d!", maxQueries));
        }
        final StringJoiner flagJoiner = new StringJoiner(", ");
        for (char c : flags) {
            flagJoiner.add(String.valueOf(c));
        }
        final String appendedFlags = flagJoiner.toString();

        final String limit;

        if (maxQueries != -1) {
            limit = String.format("LIMIT(%d) ", maxQueries);
        } else {
            limit = "";

        }

        final StringBuilder base = new StringBuilder(" WHERE ");
        final StringJoiner constraint = new StringJoiner(" AND ");

        if (!pattern.isEmpty()) {
            constraint.add("REGEX_LIKE(%2$s, " + pattern + ", " + appendedFlags + ")");
        }
        if (rawSubjects.length != 0) {
            final StringJoiner joiner = new StringJoiner(" OR ");
            for (String s : rawSubjects) {
                joiner.add(" %3$s LIKE " + s);
            }
            constraint.add("(" + joiner.toString() + ")");
        }
        if (sessionConstraint != null) {
            final String s = "WHERE " + COLUMN_EXAM_YEAR + " %1$s %2$d ";
            final String comparator;
            switch (sessionConstraint.type) {
                case ONLY:
                    comparator = " = ";
                    break;
                case AFTER:
                    comparator = " > ";
                    break;
                case BEFORE:
                    comparator = "< ";
                    break;
                default:
                    throw new IllegalStateException("Unknown session constraint: " + sessionConstraint.type);
            }
            final String localConstraint = String.format(s, comparator, sessionConstraint.examSession.epochMilli);
            constraint.add(localConstraint);
        }

        final String rawSql = base.append(constraint.toString()).append(limit).toString();
        return String.format(rawSql, TABLE_NAME, COLUMN_TITLE, COLUMN_SUBJECT, COLUMN_EXAM_YEAR, COLUMN_RESEARCH_QUESTION, COLUMN_PDF);
    }

    private void initDatabase() throws SQLException {
        try (Connection connection = pool.getConnection(); PreparedStatement init = initStatement(connection)) {
            init.execute();
        }
    }

    private @NotNull PreparedStatement initStatement(@NotNull final Connection connection) throws SQLException {
        final String initTable = "CREATE TABLE IF NOT EXIST %1$s " +
                "%2$s TEXT NOT NULL, " +
                "%3$s TEXT NOT NULL, " +
                "%4$s TEXT NOT NULL, " +
                "%5$s BIGINT NOT NULL, " +
                "%6$s BLOB NOT NULL, " +
                "UNIQUE(%2$s, %3$s, %4$s, %5$s));";
        final String initExamYearIndex = "CREATE INDEX IF NOT EXISTS %2$s_index ON %1$s " +
                "(%2$s. %3$s, %4$s, %5$s, %6$s);";
        final String initTitleIndex = "CREATE INDEX IF NOT EXISTS %3$s_index ON %1$s " +
                "(%3$s, %2$s, %4$s, %5$s, %6$s);";
        final String initResearchQuestionIndex = "CREATE INDEX IF NOT EXISTS %4$s_index ON %1$s " +
                "(%4$s, %2$s, %3$s, %5$s, %6$s);";
        final String initSubjectIndex = "CREATE INDEX IF NOT EXISTS %5$s_index ON %1$s " +
                "(%5$s, %2$s, %3$s, %4$s, %6$s);";

        final String sql = String.format(initTable + initExamYearIndex + initTitleIndex + initResearchQuestionIndex + initSubjectIndex,
                TABLE_NAME, COLUMN_TITLE, COLUMN_SUBJECT, COLUMN_EXAM_YEAR, COLUMN_RESEARCH_QUESTION, COLUMN_PDF);
        return connection.prepareStatement(sql);
    }

    public @NotNull Essay extractEssay(@NotNull ResultSet resultSet, @NotNull Function<IndexData, Optional<Essay>> cacheResolver) throws SQLException, IOException {

        final Blob blob = resultSet.getBlob(COLUMN_PDF);
        assert blob != null;
        final byte[] rawPDF = blob.getBytes(1L, (int) blob.length());

        final String rawSubject = resultSet.getString(COLUMN_SUBJECT);
        final Optional<Subject> optionalSubject = subjectDatabase.getSubjectByName(rawSubject);

        // Just parse parse / register the subject using the parser as the indices are clearly invalid.
        if (optionalSubject.isEmpty()) {
            return parser.parseDocument(new PDFParser(new RandomAccessBuffer(rawPDF)));
        }

        final Subject subject = optionalSubject.get();
        final String title = resultSet.getString(COLUMN_TITLE);
        final String researchQuestion = resultSet.getString(COLUMN_RESEARCH_QUESTION);
        final long examSession = resultSet.getLong(COLUMN_EXAM_YEAR);
        final ExamSession session = ExamSession.of(examSession);
        final IndexData indexData = new IndexData(title, subject, researchQuestion, session);
        return new Essay(indexData, rawPDF);
    }

    public @NotNull PreparedStatement newSearch(@NotNull Connection connection, @NotNull QueryParameters parameters, int maxQueries) throws SQLException {
        final String constraint = generateSqlConstraints(parameters, maxQueries);
        final String raw = "SELECT FROM %1$s (%2$s, %3$s, %4$s, %5$s, %6$s)" + constraint + ";";
        final String sql = String.format(raw, TABLE_NAME, COLUMN_TITLE, COLUMN_SUBJECT, COLUMN_EXAM_YEAR, COLUMN_RESEARCH_QUESTION, COLUMN_PDF);
        return connection.prepareStatement(sql);
    }

    public @NotNull PreparedStatement newEntry(@NotNull final Connection connection,
                                               @NotNull final Essay essay,
                                               final boolean includePDFData) throws SQLException {
        final IndexData data = essay.getIndexData();
        final String sql;
        if (includePDFData) {
            String s = "MERGE INTO %1$s (%2$s, %3$s, %4$s, %5$s) VALUES(?, ?, ?, ?, ?);";
            sql = String.format(s, TABLE_NAME, COLUMN_TITLE, COLUMN_SUBJECT, COLUMN_EXAM_YEAR, COLUMN_RESEARCH_QUESTION);
        } else {
            String s = "MERGE INTO %1$s (%2$s, %3$s, %4$s, %5$s, %6$s) VALUES(?, ?, ?, ?, ?, ?);";
            sql = String.format(s, TABLE_NAME, COLUMN_TITLE, COLUMN_SUBJECT, COLUMN_EXAM_YEAR, COLUMN_RESEARCH_QUESTION, COLUMN_PDF);
        }
        final PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, data.getTitle());
        preparedStatement.setString(2, data.getSubject().getDisplayName());
        preparedStatement.setLong(3, data.getExamSession().epochMilli);
        preparedStatement.setString(4, data.getResearchQuestion());
        if (includePDFData) {
            final PDDocument document = essay.getDocument();
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                document.save(bos);
            } catch (IOException ex) {
                // Should never happen!
                throw new RuntimeException(ex);
            }
            preparedStatement.setBlob(5, new SerialBlob(bos.toByteArray()));
        }
        return preparedStatement;
    }

    public @NotNull PreparedStatement newDeletion(@NotNull final Connection connection, @NotNull IndexData data) throws SQLException {
        final String rawSql = "DELETE FROM %1$s WHERE %2$s=? AND %3$s=? AND %4$s=? AND %5$s;";
        final String sql = String.format(rawSql, TABLE_NAME, COLUMN_TITLE, COLUMN_SUBJECT, COLUMN_EXAM_YEAR, COLUMN_RESEARCH_QUESTION);
        final PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, data.getTitle());
        preparedStatement.setString(2, data.getSubject().getDisplayName());
        preparedStatement.setLong(3, data.getExamSession().epochMilli);
        preparedStatement.setString(4, data.getResearchQuestion());
        return preparedStatement;
    }

    public @NotNull PreparedStatement newDeletion(@NotNull final Connection connection,
                                                  @NotNull final QueryParameters parameters) throws SQLException {
        final String rawSQL = "DELETE FROM %1$s" + generateSqlConstraints(parameters, -1) + ";";
        final String sql = String.format(rawSQL, TABLE_NAME, COLUMN_TITLE, COLUMN_SUBJECT, COLUMN_EXAM_YEAR, COLUMN_RESEARCH_QUESTION);
        return connection.prepareStatement(sql);
    }

}
