package me.andrewandy.eesearcher.data;

import me.andrewandy.eesearcher.ExamSession;
import me.andrewandy.eesearcher.Subject;
import org.jetbrains.annotations.NotNull;

public class EEIndexData {

    private final Subject subject;
    private final String researchQuestion;
    private final String title;
    private final ExamSession examSession;

    public EEIndexData(@NotNull final String title, @NotNull final Subject subject,
                       @NotNull final String researchQuestion, @NotNull final ExamSession examSession) {
        this.subject = subject;
        this.title = title;
        this.researchQuestion = researchQuestion;
        this.examSession = examSession;
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


}
