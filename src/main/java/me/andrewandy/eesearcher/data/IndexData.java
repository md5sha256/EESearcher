package me.andrewandy.eesearcher.data;

import me.andrewandy.eesearcher.ExamSession;
import org.jetbrains.annotations.NotNull;

public class IndexData {

    public final int uniqueID;
    private final Subject subject;
    private final String researchQuestion;
    private final String title;
    private final ExamSession examSession;

    public IndexData(@NotNull final String title,
                     @NotNull final Subject subject,
                     @NotNull final String researchQuestion,
                     @NotNull final ExamSession examSession) {
        this.subject = subject;
        this.title = title;
        this.researchQuestion = researchQuestion;
        this.examSession = examSession;
        this.uniqueID = hashCode();
    }

    public int getUniqueID() {
        return uniqueID;
    }

    public Subject getSubject() {
        return subject;
    }

    public String getTitle() {
        return title;
    }

    public String getResearchQuestion() {
        return researchQuestion;
    }

    public ExamSession getExamSession() {
        return examSession;
    }

    @Override
    public String toString() {
        return "IndexData{" +
                "subject=" + subject +
                ", researchQuestion='" + researchQuestion + '\'' +
                ", title='" + title + '\'' +
                ", examSession=" + examSession +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IndexData indexData = (IndexData) o;

        if (!subject.equals(indexData.subject)) return false;
        if (!researchQuestion.equals(indexData.researchQuestion)) return false;
        if (!title.equals(indexData.title)) return false;
        return examSession.equals(indexData.examSession);
    }

    @Override
    public int hashCode() {
        int result = subject.hashCode();
        result = 31 * result + researchQuestion.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + examSession.hashCode();
        return result;
    }
}
