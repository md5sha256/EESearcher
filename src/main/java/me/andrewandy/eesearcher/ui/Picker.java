package me.andrewandy.eesearcher.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.*;

public class Picker extends Application {

    // Cached so we have O(1) lookup times as opposed to a ListView#getItems's O(n) lookup
    private final Set<File> fileCache = new HashSet<>();

    // UI Variables
    private final VBox listBox = new VBox();
    private final VBox rootGroup = new VBox(12);
    private final GridPane pane = new GridPane();
    private final ListView<File> listView = new ListView<>();
    private final Button buttonAdd = new Button("Add");
    private final Button buttonRemove = new Button("Delete");
    private final Button buttonMoveUp = new Button("Move Up");
    private final Button buttonMoveDown = new Button("Move Down");
    private final Button buttonClearSel = new Button("Clear Selection");
    private final TilePane paneEditFiles = new TilePane();
    private final Label listLabel = new Label("Selected EEs");
    private final Label status = new Label(" ");
    private final Label info = new Label("Select files by dragging and dropping or importing.");

    // Lazy init for when #start is called
    private Stage stage;

    // Stateful variables
    private SelectionState selectionState = SelectionState.EMPTY;

    public static void main(String[] args) {
        launch(args);
    }

    private static void configureFileChooser(final FileChooser fileChooser) {
        fileChooser.setTitle("View Pictures");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
    }


    private static void enableButton(final Button... buttons) {
        for (Button button : buttons) {
            button.setDisable(false);
        }
    }

    private static void disableButton(final Button... buttons) {
        for (Button button : buttons) {
            button.setDisable(true);
        }
    }

    @Override
    public void start(final Stage stage) {
        this.stage = stage;
        initStage();
        draw();
    }

    public List<File> getPickedFiles() {
        return new ArrayList<>(this.listView.getItems());
    }

    public Set<File> getPickedFilesUnordered() {
        return new HashSet<>(this.fileCache);
    }


    public int addFiles(final Collection<File> files) {
        final List<File> toAdd = new ArrayList<>(files.size());
        for (File file : files) {
            if (fileCache.add(file)) {
                toAdd.add(file);
            }
        }
        listView.getItems().addAll(toAdd);
        analyseState();
        evaluateState();
        return toAdd.size();
    }

    public int removeFiles(final Collection<File> files) {
        final List<File> removed = new ArrayList<>(files);
        for (File file : files) {
            if (this.fileCache.remove(file)) {
                removed.add(file);
            }
        }
        listView.getItems().removeAll(removed);
        analyseState();
        evaluateState();
        return removed.size();
    }

    public void clearFiles() {
        this.fileCache.clear();
        listView.getItems().clear();
        this.selectionState = SelectionState.EMPTY;
        // No need to analyse the state
        evaluateState();
    }

    public void moveCursor(int amount) {
        if (amount == 0 || listView.getItems().isEmpty()) {
            return;
        }
        final int selectedIndex = listView.getSelectionModel().getSelectedIndex();
        final MultipleSelectionModel<File> selectionModel = listView.getSelectionModel();
        final int newIndex;
        // Extract common if-else when evaluating newIndex
        this.selectionState = null;
        if (amount < 0) {
            newIndex = Math.max(0, selectedIndex + amount);
            if (newIndex == 0) {
                this.selectionState = SelectionState.FIRST_SELECTED;
            }
        } else {
            newIndex = Math.min(listView.getItems().size() - 1, selectedIndex + amount);
            if (newIndex == listView.getItems().size() - 1) {
                this.selectionState = SelectionState.LAST_SELECTED;
            }
        }
        this.selectionState = this.selectionState == null ? SelectionState.ELEMENT_SELECTED : this.selectionState;
        selectionModel.select(newIndex);
        // No need to analyse the state
        evaluateState();
    }

    public void evaluateCursorClick() {
        final List<File> list = listView.getItems();
        if (list.isEmpty()) {
            this.selectionState = SelectionState.EMPTY;
            evaluateState();
            return;
        }
        final MultipleSelectionModel<File> selectionModel = listView.getSelectionModel();
        if (selectionModel.getSelectedItems().size() == list.size()) {
            this.selectionState = SelectionState.ALL_SELECTED;
            evaluateState();
            return;
        }
        final List<File> selected = selectionModel.getSelectedItems();
        final int selectedIndex = selectionModel.getSelectedIndex();
        if (selectedIndex == 0) {
            this.selectionState = selected.size() == 1 ? SelectionState.FIRST_SELECTED : SelectionState.MULTI_FIRST_SELECTED;
        } else if (selectedIndex == list.size() - 1) {
            this.selectionState = selected.size() == 1 ? SelectionState.LAST_SELECTED : SelectionState.MULTI_LAST_SELECTED;
        } else {
            this.selectionState = SelectionState.MULTI_SELECTED;
        }
        evaluateState();
    }

    private void draw() {
        stage.setTitle("Add Extended Essays");
        stage.setScene(new Scene(rootGroup));
        stage.show();
    }

    private void initStage() {
        initView();
        initLogic();
        evaluateState();
    }

    private void initLogic() {

        buttonClearSel.setOnAction(event -> {
            clearFiles();
            displayInfo(Color.GREEN, "Selection cleared!");
            event.consume();
        });

        buttonAdd.setOnAction(event -> {
            final FileChooser chooser = new FileChooser();
            configureFileChooser(chooser);
            final List<File> files = chooser.showOpenMultipleDialog(stage);
            int added = files == null ? 0 : addFiles(files);
            if (added == 0) {
                displayInfo(Color.BLACK, "No files added");
            } else {
                displayInfo(Color.GREEN, String.format("%d files added!", added));
            }
            event.consume();
        });

        buttonRemove.setOnAction(event -> {
            final List<File> selection = listView.getSelectionModel().getSelectedItems();
            if (selection.size() == 0) {
                event.consume();
                return;
            }
            //assert selection != null && !selection.isEmpty();
            int removed = removeFiles(selection);
            displayInfo(Color.GREEN, String.format("%d Files removed!", removed));

            event.consume();
        });

        buttonMoveUp.setOnMouseClicked(event -> {
            int toMove = event.isShiftDown() ? 10 : 1;
            moveCursor(-toMove);
            event.consume();
        });

        buttonMoveUp.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                int toMove = event.isShiftDown() ? 10 : 1;
                moveCursor(-toMove);
                event.consume();
            }
        });

        buttonMoveDown.setOnMouseClicked(event -> {
            int toMove = event.isShiftDown() ? 10 : 1;
            moveCursor(toMove);
            event.consume();
        });

        buttonMoveDown.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                int toMove = event.isShiftDown() ? 10 : 1;
                moveCursor(toMove);
                event.consume();
            }
        });

        listView.setOnMouseClicked(event -> {
            final MultipleSelectionModel<File> selectionModel = listView.getSelectionModel();
            if (event.isShiftDown()) {
                selectionModel.setSelectionMode(SelectionMode.MULTIPLE);
            } else {
                selectionModel.setSelectionMode(SelectionMode.SINGLE);
            }
            evaluateCursorClick();
            event.consume();
        });

        listView.setOnKeyPressed(event -> {
            final MultipleSelectionModel<File> selectionModel = listView.getSelectionModel();
            final SelectionMode mode = selectionModel.getSelectionMode();
            if (event.isShiftDown()) {
                selectionModel.setSelectionMode(SelectionMode.MULTIPLE);
            } else {
                switch (event.getCode()) {
                    case UP:
                    case DOWN:
                        selectionModel.setSelectionMode(SelectionMode.SINGLE);
                    default:
                        break;
                }
            }
            analyseState();
            evaluateState();
            event.consume();
        });

        listView.setOnDragOver(event -> {
            final Dragboard dragboard = event.getDragboard();
            for (File file : dragboard.getFiles()) {
                // Make sure we don't already have the file
                if (fileCache.contains(file)) {
                    event.acceptTransferModes();
                    event.consume();
                    return;
                }
            }
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            event.consume();
        });

        listView.setOnDragDropped(event -> {
            final Dragboard dragboard = event.getDragboard();
            List<File> files = dragboard.getFiles();
            int added = files == null ? 0 : addFiles(files);
            if (added == 0) {
                displayInfo(Color.BLACK, "No files added");
            } else {
                displayInfo(Color.GREEN, String.format("%d files added!", added));
            }
            event.setDropCompleted(true);
            event.consume();
        });

    }

    private void initView() {
        info.setTextFill(Color.BLACK);

        // Init ListView
        listLabel.setAlignment(Pos.CENTER);
        listLabel.setPadding(new Insets(10, 0, 10, 0));
        listView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        listBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        listBox.getChildren().addAll(listLabel, listView);

        // Init button colours
        buttonAdd.setTextFill(Color.BLACK);
        buttonRemove.setTextFill(Color.BLACK);
        buttonMoveUp.setTextFill(Color.BLACK);
        buttonMoveDown.setTextFill(Color.BLACK);
        buttonClearSel.setTextFill(Color.BLACK);

        // Init button sizes
        buttonAdd.setMaxWidth(Double.MAX_VALUE);
        buttonRemove.setMaxWidth(Double.MAX_VALUE);
        buttonMoveUp.setMaxWidth(Double.MAX_VALUE);
        buttonMoveDown.setMaxWidth(Double.MAX_VALUE);
        buttonClearSel.setMaxWidth(Double.MAX_VALUE);

        // Init pane for buttons
        paneEditFiles.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        paneEditFiles.setPadding(new Insets(40, 20, 10, 20));
        paneEditFiles.setVgap(10);
        paneEditFiles.setAlignment(Pos.TOP_CENTER);
        paneEditFiles.setOrientation(Orientation.VERTICAL);
        paneEditFiles.getChildren().addAll(buttonAdd, buttonRemove, buttonMoveUp, buttonMoveDown, buttonClearSel);


        pane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        pane.add(listBox, 0, 0);
        pane.add(paneEditFiles, 1, 0);
        pane.add(status, 0, 1);
        pane.add(info, 0, 2);

        rootGroup.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        rootGroup.getChildren().add(pane);
        //rootGroup.getChildren().addAll(listBox, paneEditFiles, info);
        rootGroup.setPadding(new Insets(12, 12, 12, 12));
    }

    private void analyseState() {
        final List<File> items = listView.getItems();
        if (items.isEmpty()) {
            this.selectionState = SelectionState.EMPTY;
            return;
        }
        final MultipleSelectionModel<File> selectionModel = listView.getSelectionModel();
        if (selectionModel.getSelectedItems().isEmpty()) {
            this.selectionState = SelectionState.NULL_CURSOR;
            return;
        }
        if (selectionModel.getSelectedItems().size() == listView.getItems().size()) {
            this.selectionState = SelectionState.ALL_SELECTED;
            return;
        }
        final int selectedIndex = selectionModel.getSelectedIndex();
        if (selectionModel.getSelectionMode() == SelectionMode.MULTIPLE) {
            if (selectedIndex == 0 || selectionModel.getSelectedIndices().contains(0)) {
                this.selectionState = SelectionState.MULTI_FIRST_SELECTED;
            } else if (selectedIndex == items.size() - 1 || selectionModel.getSelectedIndices().contains(items.size() - 1)) {
                this.selectionState = SelectionState.MULTI_LAST_SELECTED;
            } else {
                this.selectionState = SelectionState.MULTI_SELECTED;
            }
            return;
        }
        if (selectedIndex == 0) {
            this.selectionState = SelectionState.FIRST_SELECTED;
        } else if (selectedIndex == items.size() - 1) {
            this.selectionState = SelectionState.LAST_SELECTED;
        } else {
            this.selectionState = SelectionState.ELEMENT_SELECTED;
        }
    }

    private void evaluateState() {
        final List<File> inView = listView.getItems();
        if (inView.isEmpty()) {
            disableButton(buttonMoveUp, buttonMoveDown, buttonRemove, buttonClearSel);
        } else {
            enableButton(buttonClearSel);
        }
        if (!listView.getSelectionModel().getSelectedItems().isEmpty()) {
            enableButton(buttonRemove);
        } else {
            disableButton(buttonRemove);
        }
        switch (this.selectionState) {
            case NULL_CURSOR:
                // Cannot move around, cannot remove
                enableButton(buttonAdd);
                disableButton(buttonMoveDown, buttonMoveUp, buttonRemove);
            case EMPTY:
                // Cannot clear selection
                disableButton(buttonClearSel);
                break;
            case ALL_SELECTED:
                // Cannot move up or down
                disableButton(buttonMoveDown, buttonMoveUp);
                break;
            case FIRST_SELECTED:
            case MULTI_FIRST_SELECTED:
                // Cannot move up
                enableButton(buttonMoveDown);
                disableButton(buttonMoveUp);
                break;
            case LAST_SELECTED:
            case MULTI_LAST_SELECTED:
                // Cannot move down
                enableButton(buttonMoveUp);
                disableButton(buttonMoveDown);
            default:
                // Do nothing
                break;
        }
    }

    private void displayInfo(final Color color, final String message) {
        info.setText(" ");
        info.setTextFill(color);
        info.setText(message);
    }

    private enum SelectionState {
        EMPTY, NULL_CURSOR, ELEMENT_SELECTED, FIRST_SELECTED, LAST_SELECTED, MULTI_SELECTED, MULTI_FIRST_SELECTED, MULTI_LAST_SELECTED, ALL_SELECTED
    }

}
