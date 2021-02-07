package me.andrewandy.eesearcher.data;

import com.google.inject.Inject;
import me.andrewandy.eesearcher.ExamSession;
import me.andrewandy.eesearcher.Utils;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    private static final Pattern SUBJECT_PARSER = Pattern.compile("(^Subject:)(\\s?(\\w+|.|\\s)+($|\\.))", Pattern.CASE_INSENSITIVE);
    private static final Pattern RESEARCH_QUESTION_PARSER = Pattern.compile("(Research Question:|^)(\\s?(.|\\s)+($|\\?))", Pattern.CASE_INSENSITIVE);
    private static final Pattern TITLE_PARSER = Pattern.compile("(^(Topic|Title):)(\\s?(\\w+|.|\\s)+($|\\.))", Pattern.CASE_INSENSITIVE);
    private static final Pattern EXAM_SESSION_PARSER = Pattern.compile("^((may|november)(\\s?([0-9]{4})))", Pattern.CASE_INSENSITIVE);
    private static final Pattern LANGUAGE_PATTERN = Pattern.compile("^(\\w+)(\\s?([ab])|$|\\.)");

    @Inject
    private SubjectDatabase database;

    public Essay parseDocument(final PDFParser parser) throws IOException, IllegalArgumentException {
        final List<String> pages = parseTextByPage(parser);
        final String coverPage = pages.get(0);
        final Matcher subjectMatcher = SUBJECT_PARSER.matcher(coverPage);
        if (!subjectMatcher.find()) {
            throw new IllegalArgumentException("Invalid Essay: No title/topic found!");
        }

        final Matcher titleMatcher = TITLE_PARSER.matcher(coverPage);
        if (!titleMatcher.find()) {
            throw new IllegalArgumentException("Invalid Essay: No title/topic found!");
        }
        final String title = titleMatcher.group(3);

        final Matcher rqMatcher = RESEARCH_QUESTION_PARSER.matcher(coverPage);
        if (!rqMatcher.find()) {
            throw new IllegalArgumentException("Invalid Essay: No title/topic found!");
        }
        final String researchQuestion = rqMatcher.group(2);

        final String rawSubject = subjectMatcher.group(2);
        final Matcher languageMatcher = LANGUAGE_PATTERN.matcher(rawSubject);
        final Subject subject;
        if (languageMatcher.find()) {
            final String language = languageMatcher.group(1);
            final Optional<Subject> optional = database.getSubjectByName(language);
            subject = optional.orElseGet(() -> {
                final Subject newSubject = new Subject((byte) 1, Utils.titleCase(language.toLowerCase(Locale.ROOT)), true);
                database.registerSubject(newSubject);
                return newSubject;
            });
        } else {
            subject = database.getSubjectByName(subjectMatcher.group(2)).orElseThrow(() -> new IllegalArgumentException(String.format("Invalid Subject: %s", rawSubject)));
        }
        final ExamSession session;
        final Matcher sessionMatcher = EXAM_SESSION_PARSER.matcher(coverPage);
        if (sessionMatcher.find()) {
            final String rawMonth = sessionMatcher.group(2);
            final String rawYear = sessionMatcher.group(3).trim();
            ExamSession temp;
            try {
                Month month = Month.valueOf(rawMonth.toUpperCase(Locale.ENGLISH));
                int year = Integer.parseInt(rawYear);
                temp = ExamSession.of(month, year);
            } catch (IllegalArgumentException ex) {
                temp = ExamSession.EMPTY_SESSION;
            }
            session = temp;
        } else {
            session = ExamSession.EMPTY_SESSION;
        }
        final IndexData data = new IndexData(title, subject, researchQuestion, session);
        return new Essay(data, parser.getPDDocument());
    }

    public PDFParser parseDocument(final InputStream inputStream) throws IOException {
        final PDFParser parser = new PDFParser(new RandomAccessBuffer(inputStream));
        parser.parse();
        return parser;
    }

    public String parseText(final InputStream inputStream) throws IOException {
        return parseText(parseDocument(inputStream));
    }

    public String parseText(final PDFParser parser) throws IOException {
        final PDFTextStripper pdfStripper = new PDFTextStripper();
        try (final PDDocument document = parser.getPDDocument()) {
            return pdfStripper.getText(document);
        }
    }

    public List<String> parseTextByPage(final PDFParser parser) throws IOException {
        try (final PDDocument document = parser.getPDDocument()) {
            final PDFTextStripper stripper = new PDFTextStripper();
            final List<String> pages = new ArrayList<>(document.getNumberOfPages());
            for (int i = 1; i < document.getNumberOfPages(); ) {
                stripper.setStartPage(i++);
                stripper.setEndPage(i);
                pages.add(stripper.getText(document));
            }
            return pages;
        }
    }

}
