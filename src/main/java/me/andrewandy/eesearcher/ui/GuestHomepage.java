package me.andrewandy.eesearcher.ui;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ListView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import me.andrewandy.eesearcher.SearchHistoryController;
import me.andrewandy.eesearcher.SubjectDatabase;

public class GuestHomepage {

    private static final Border DEF_BORDER = new Border(new BorderStroke(Color.BLACK,
            BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));

    private SubjectDatabase subjectDatabase;
    private Pane pane;
    private SearchHistoryController historyController;

    public GuestHomepage() {

    }

    public Pane getPane() {
        if (pane == null) {
            init();
        }
        return pane;
    }

    public void init() {

        final StackPane root = new StackPane();
        root.setPadding(new Insets(12, 12, 12, 12));
        root.setAlignment(Pos.CENTER);
        root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);


        final ListView<String> listViewSearchHistory = new ListView<>();
        listViewSearchHistory.setBackground(Background.EMPTY);
        listViewSearchHistory.setBorder(DEF_BORDER);
        listViewSearchHistory.setPadding(new Insets(10, 0, 10, 0));
        listViewSearchHistory.getItems().addAll(historyController.getHistory());

        final ListView<String> listViewResults = new ListView<>();
        listViewResults.setBorder(DEF_BORDER);
        listViewResults.setPadding(new Insets(10, 10, 10, 10));

        this.pane = root;
    }


}
