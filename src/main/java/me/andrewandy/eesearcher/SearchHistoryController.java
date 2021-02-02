package me.andrewandy.eesearcher;

import javafx.collections.ObservableList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class SearchHistoryController implements Serializable {

    private LinkedList<String> history = new LinkedList<>();

    public SearchHistoryController() {

    }

    public List<String> getHistory() {
        return new ArrayList<>();
    }

    public void offerHistory(final List<String> history) {
        this.history = new LinkedList<>(history);
    }

    public void addEntry(final String entry) {
        this.history.add(Objects.requireNonNull(entry));
    }

    public String removeEntry() {
        return history.remove();
    }
}
