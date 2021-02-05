package me.andrewandy.eesearcher.ui;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import me.andrewandy.eesearcher.SearchController;
import me.andrewandy.eesearcher.SearchHistoryController;
import me.andrewandy.eesearcher.SubjectDatabase;
import me.andrewandy.eesearcher.data.EEIndexData;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Pattern;

public class GuestHomepage extends Application {

    private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private static final Border DEF_BORDER = new Border(new BorderStroke(Color.BLACK,
            BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));


    private final VBox root = new VBox();
    private final HBox boxInfo = new HBox();
    private final Label info = new Label(" ");
    private final ProgressBar progressBar = new ProgressBar();
    private final SplitPane paneCentralView = new SplitPane();
    private final TitledPane paneSearchHistory = new TitledPane();

    private final TreeView<Hyperlink> viewSearchHistory = new TreeView<>(new TreeItem<>());
    private final TextField fieldSearchInput = new TextField(" ");
    private final TitledPane paneSearchResults = new TitledPane();
    private final TreeView<TextFlow> viewSearchResults = new TreeView<>(new TreeItem<>());
    private final SearchController searchController = new SearchController();
    private final SearchHistoryController historyController = new SearchHistoryController();

    private Stage stage;
    private SubjectDatabase subjectDatabase;
    private boolean searching;


    public static void main(String[] args) {
        launch(args);
    }

    private static void changeFontWeight(final Text text, final FontWeight weight) {
        final Font original = text.getFont();
        final Font newFont = Font.font(original.getFamily(), weight, original.getSize());
        text.setFont(newFont);
    }

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        initStage();
        draw();
    }

    public void draw() {
        this.stage.setTitle("Extended Essay Searcher");
        JMetro jMetro = new JMetro(Style.LIGHT);
        final Scene scene = new Scene(this.root);
        jMetro.setScene(scene);
        jMetro.reApplyTheme();
        stage.setScene(scene);
        this.stage.show();
    }

    public void initStage() {
        initView();
        initLogic();
    }

    public void initLogic() {
        fieldSearchInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                performSearch(fieldSearchInput.getText());
            }
            event.consume();
        });
    }

    public void initView() {
        stage.setResizable(true);

        // Init root
        root.setPadding(new Insets(12, 12, 12, 12));
        root.setSpacing(10);
        root.setAlignment(Pos.CENTER);
        root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        fieldSearchInput.setMaxSize(Double.MAX_VALUE, Control.USE_PREF_SIZE);

        /// Init search history display
        viewSearchHistory.setBackground(Background.EMPTY);
        //viewSearchHistory.setBorder(DEF_BORDER);
        viewSearchHistory.setPadding(Insets.EMPTY);
        viewSearchHistory.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Init search history pane
        paneSearchHistory.setText("Search History");
        paneSearchHistory.setTextFill(Color.WHITE);
        paneSearchHistory.setBackground(new Background(new BackgroundFill(Color.RED, new CornerRadii(5), Insets.EMPTY)));
        paneSearchHistory.setCollapsible(false);
        paneSearchHistory.setContent(viewSearchHistory);
        paneSearchHistory.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        viewSearchResults.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        paneSearchResults.setText("Results");
        paneSearchResults.setTextFill(Color.RED);
        //paneSearchResults.setBorder(DEF_BORDER);
        paneSearchResults.setCollapsible(false);
        paneSearchResults.setContent(viewSearchResults);
        paneSearchResults.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);


        paneCentralView.setPadding(Insets.EMPTY);
        paneCentralView.getItems().addAll(paneSearchHistory, paneSearchResults);
        paneCentralView.setDividerPositions(0.25f, 0.75f);
        paneCentralView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            double[] positions = paneCentralView.getDividerPositions(); // reccord the current ratio
            Platform.runLater(() -> paneCentralView.setDividerPositions(positions)); // apply the now former ratio
        });
        info.setPadding(Insets.EMPTY);

        progressBar.setPadding(Insets.EMPTY);
        progressBar.setMaxSize(Double.MAX_VALUE, 15);
        progressBar.setVisible(false);

        boxInfo.setSpacing(10);
        HBox.setHgrow(info, Priority.ALWAYS);
        HBox.setHgrow(progressBar, Priority.ALWAYS);
        boxInfo.getChildren().addAll(info, progressBar);
        root.getChildren().addAll(fieldSearchInput, paneCentralView, boxInfo);
    }

    private void performSearch(@NotNull final String search) {
        if (search.isBlank()) {
            return;
        }
        if (searching) {
            return;
        }
        historyController.addEntry(search);
        info.setText("Searching... ");
        searching = true;
        progressBar.setVisible(true);
        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        final SearchController.SearchQuery query = new SearchController.SearchQuery(SearchController.SearchAttribute.TEXT,
                // Force UTF 8
                // Potential performance improvement if we can cache queries + lookup cached queries to see if they need to be made.
                Pattern.compile(new String(search.getBytes(StandardCharsets.UTF_8)).toLowerCase(Locale.ROOT)));
        searchController.performQuery(query).thenAccept(result -> Platform.runLater(() -> {
            processSearchResultEntries(result);
            progressBar.setVisible(false);
            searching = false;
            info.setText(" ");
        }));
    }

    private void processSearchResultEntries(@NotNull final SearchController.SearchResult result) {
        final List<String> correctText = result.getMatchingText();
        // FIXME keyword highlighting
        final EEIndexData indexData = result.getIndexData();
        final Map<SearchController.SearchAttribute, String> attributes = result.getAttributes();
        // Parse values
        final String subject = attributes.get(SearchController.SearchAttribute.SUBJECT);
        final String examSession = attributes.get(SearchController.SearchAttribute.EXAM_SESSION);
        final String elementDisplayName = String.format("$1%s | $2%s", subject, examSession);
        final String title = indexData.getTitle();
        final String researchQuestion = indexData.getResearchQuestion();

        final TreeItem<TextFlow> rootItem = this.viewSearchResults.getRoot();

        // Begin adding elements to search history
        final TreeItem<TextFlow> newItem = new TreeItem<>();
        final TextFlow entryTitleFlow = new TextFlow();
        final Text entryTitle = new Text(elementDisplayName);
        changeFontWeight(entryTitle, FontWeight.BOLD);
        newItem.setValue(entryTitleFlow);

        final TreeItem<TextFlow> infoItem = new TreeItem<>();
        final TextFlow entryInfoFlow = new TextFlow();
        final Text textEssayTitleIdentifier = new Text("Title: ");
        changeFontWeight(textEssayTitleIdentifier, FontWeight.BOLD);
        textEssayTitleIdentifier.setFill(Color.DARKGRAY);
        final Text textEssayTitle = new Text(title + "\n");
        final Text textEssayRQIdentifier = new Text("Research Question: ");
        changeFontWeight(textEssayRQIdentifier, FontWeight.BOLD);
        final Text textRQ = new Text(researchQuestion + "\n");
        entryInfoFlow.getChildren().addAll(textEssayTitleIdentifier, textEssayTitle, textEssayRQIdentifier, textRQ);
        for (String s : correctText) {
            entryInfoFlow.getChildren().add(new Text(s + "\n"));
        }
        infoItem.setValue(entryInfoFlow);
        rootItem.getChildren().add(0, newItem);
    }

    public void onSearchHistoryUpdate() {

    }


}
