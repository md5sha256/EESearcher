package me.andrewandy.eesearcher.data;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SearchResult {

    private final Essay essay;
    private final List<String> matchingText;

    public SearchResult(@NotNull final Essay essay, @NotNull final List<String> matchingText) {
        this.essay = essay;
        this.matchingText = new ArrayList<>(matchingText);
    }

    public @NotNull Essay getEssay() {
        return essay;
    }

    public List<String> getMatchingText() {
        return matchingText;
    }
}
