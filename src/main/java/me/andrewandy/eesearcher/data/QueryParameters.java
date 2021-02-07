package me.andrewandy.eesearcher.data;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class QueryParameters {

    public final String regex;
    public final char[] flags;
    public final boolean deepSearch;
    public final ExamSessionConstraint examSessionConstraint;
    public final Set<Subject> subjects;

    private QueryParameters(@Nullable String regex, char[] flags, boolean deepSearch, @Nullable ExamSessionConstraint examSessionConstraint, @Nullable Set<Subject> subjects) {
        this.regex = regex;
        this.flags = flags;
        this.deepSearch = deepSearch;
        this.examSessionConstraint = examSessionConstraint;
        this.subjects = subjects;
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public @NotNull Builder toBuilder() {
        return new Builder(this);
    }

    public static class Builder {

        private String regex;
        private char[] flags;
        private boolean deepSearch;
        private ExamSessionConstraint examSessionConstraint;
        private Set<Subject> subjects;

        private Builder() {
            reset();
        }

        private Builder(QueryParameters parameters) {
            this.regex = parameters.regex;
            this.deepSearch = parameters.deepSearch;
            this.flags = Arrays.copyOf(parameters.flags, parameters.flags.length);
            this.examSessionConstraint = new ExamSessionConstraint(parameters.examSessionConstraint);
            this.subjects = parameters.subjects == null ? null : new HashSet<>(parameters.subjects);
        }

        public Builder regex(@Nullable String regex) {
            this.regex = regex;
            return this;
        }

        public Builder regexFlags(char... flags) {
            this.flags = flags == null ? new char[0] : flags;
            return this;
        }

        public Builder deepSearch(boolean deep) {
            this.deepSearch = deep;
            return this;
        }

        public Builder examSessionConstrain(ExamSessionConstraint constraint) {
            this.examSessionConstraint = constraint;
            return this;
        }

        public Builder subjects(Subject... subjects) {
            this.subjects = subjects == null ? null : new HashSet<>(Arrays.asList(subjects));
            return this;
        }

        public Builder subjects(Collection<Subject> subjects) {
            this.subjects = subjects == null ? null : new HashSet<>(subjects);
            return this;
        }

        public Builder reset() {
            this.regex = null;
            this.flags = new char[0];
            this.deepSearch = false;
            this.examSessionConstraint = null;
            this.subjects = null;
            return this;
        }

        public QueryParameters build() {
            return new QueryParameters(regex, flags, deepSearch, examSessionConstraint, subjects);
        }
    }
}
