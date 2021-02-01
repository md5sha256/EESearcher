package me.andrewandy.eesearcher.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Picker extends Application {

    private final Desktop desktop = Desktop.getDesktop();

    private final List<File> files = new LinkedList<>();
    private final Set<File> fileCache = new HashSet<>();

    public static void main(String[] args) {
        launch(args);
    }

    private static void configureFileChooser(final FileChooser fileChooser) {
        fileChooser.setTitle("View Pictures");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
    }

    public List<File> getPickedFiles() {
        return files;
    }

    @Override
    public void start(final Stage stage) {
        stage.setTitle("Add Extended Essays");

        final VBox listBox = new VBox();
        final Label listLabel = new Label("Selected EEs");
        listLabel.setAlignment(Pos.CENTER);
        listLabel.setPadding(new Insets(10, 0, 10, 0));
        final ListView<File> listView = new ListView<>();
        listView.getItems().addAll(files);
        listView.setPrefSize(100, 300);
        listBox.getChildren().addAll(listLabel, listView);

        final Button buttonAdd = new Button("Add");
        final Button buttonRemove = new Button("Delete");
        final Button buttonMoveUp = new Button("Move Up");
        final Button buttonMoveDown = new Button("Move Down");
        final Button buttonClearSel = new Button("Clear Selection");

        buttonAdd.setTextFill(Color.BLACK);
        buttonRemove.setTextFill(Color.BLACK);
        buttonMoveUp.setTextFill(Color.BLACK);
        buttonMoveDown.setTextFill(Color.BLACK);
        buttonClearSel.setTextFill(Color.BLACK);

        buttonAdd.setMaxWidth(Double.MAX_VALUE);
        buttonRemove.setMaxWidth(Double.MAX_VALUE);
        buttonMoveUp.setMaxWidth(Double.MAX_VALUE);
        buttonMoveDown.setMaxWidth(Double.MAX_VALUE);
        buttonClearSel.setMaxWidth(Double.MAX_VALUE);

        final TilePane paneEditFiles = new TilePane();
        paneEditFiles.setPadding(new Insets(0, 20, 10, 20));
        paneEditFiles.setVgap(10);
        paneEditFiles.setAlignment(Pos.CENTER);
        paneEditFiles.setOrientation(Orientation.VERTICAL);
        paneEditFiles.getChildren().addAll(buttonAdd, buttonRemove, buttonMoveUp, buttonMoveDown, buttonClearSel);

        final Label status = new Label(" ");

        buttonClearSel.setOnAction(event -> {
            listView.getItems().clear();
            this.files.clear();
            this.fileCache.clear();
            status.setTextFill(Color.GREEN);
            status.setText("Selection cleared!");
            event.consume();
        });
        final Label info = new Label("Select files by dragging and dropping or importing.");
        info.setTextFill(Color.BLACK);

        buttonAdd.setOnMouseClicked(event -> {
            final FileChooser chooser = new FileChooser();
            configureFileChooser(chooser);
            final List<File> files = chooser.showOpenMultipleDialog(stage);
            int added = 0;
            if (files != null) {
                for (File f : files) {
                    if (!fileCache.contains(f)) {
                        this.files.add(f);
                        listView.getItems().add(f);
                        added++;
                    }
                }
                this.fileCache.addAll(files);
                status.setTextFill(Color.GREEN);
                status.setText(String.format("%d Files added!", added));
            }
            if (added == 0) {
                status.setTextFill(Color.BLACK);
                status.setText("No Files Added");
            } else {
                status.setTextFill(Color.GREEN);
                status.setText(String.format("%d Files added!", added));
                enableButton(buttonClearSel);
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
            final int lastSelIndex = listView.getSelectionModel().getSelectedIndex();
            final int firstSelIndex = lastSelIndex - selection.size();
            this.files.removeAll(selection);
            this.fileCache.removeAll(selection);
            listView.getItems().removeAll(selection);
            status.setTextFill(Color.GREEN);
            status.setText(String.format("%d Files removed!", selection.size()));
            if (firstSelIndex == 0) {
                listView.getSelectionModel().selectFirst();
            } else {
                listView.getSelectionModel().select(firstSelIndex);
            }
            event.consume();
        });

        buttonMoveUp.setOnMouseClicked(event -> {
            final int index;
            final int firstRow = 0;
            if (listView.getSelectionModel().getSelectionMode() == SelectionMode.SINGLE) {
                index = listView.getSelectionModel().getSelectedIndex();
            } else {
                index = listView.getSelectionModel().getSelectedIndices().stream().min(Integer::compareTo).orElse(0);
            }
            if (index == 0) {
                event.consume();
                return;
            }
            int toMove = event.isShiftDown() ? 10 : 1;
            listView.getSelectionModel().select(Math.max(firstRow, index - toMove));
            final int selectedIndex = listView.getSelectionModel().getSelectedIndex();
            if (selectedIndex == 0) {
                disableButton(buttonMoveUp);
            } else {
                enableButton(buttonMoveDown);
            }
            event.consume();
        });

        buttonMoveDown.setOnMouseClicked(event -> {
            final int index;
            int lastRow = this.files.size() - 1;
            if (listView.getSelectionModel().getSelectionMode() == SelectionMode.SINGLE) {
                index = listView.getSelectionModel().getSelectedIndex();
            } else {
                index = listView.getSelectionModel().getSelectedIndices().stream().max(Integer::compareTo).orElse(lastRow);
            }
            if (index == lastRow) {
                event.consume();
                return;
            }
            int toMove = event.isShiftDown() ? 10 : 1;
            listView.getSelectionModel().select(Math.min(lastRow, index + toMove));
            final int selected = listView.getSelectionModel().getSelectedIndex();
            if (selected == lastRow) {
                disableButton(buttonMoveDown);
            } else {
                enableButton(buttonMoveUp);
            }
            event.consume();
        });

        listView.setOnMouseClicked(event -> {
            final int index = listView.getSelectionModel().getSelectedIndex();
            if (this.files.size() <= 1) {
                disableButton(buttonMoveUp);
                disableButton(buttonMoveDown);
                event.consume();
                return;
            }
            if (index == 0) {
                disableButton(buttonMoveUp);
                enableButton(buttonMoveDown);
            } else if (index == this.files.size() - 1) {
                disableButton(buttonMoveDown);
                enableButton(buttonMoveUp);
            } else {
                enableButton(buttonMoveUp);
                enableButton(buttonMoveDown);
            }
            event.consume();
        });

        if (this.files.isEmpty()) {
            disableButton(buttonRemove);
            disableButton(buttonMoveDown);
            disableButton(buttonMoveUp);
            disableButton(buttonClearSel);
        }

        final VBox rootGroup = new VBox(12);
        final GridPane pane = new GridPane();
        pane.add(listBox, 0, 0);
        pane.add(paneEditFiles, 1, 0);
        pane.add(status, 0, 1);
        pane.add(info, 0, 2);
        rootGroup.getChildren().add(pane);
        //rootGroup.getChildren().addAll(listBox, paneEditFiles, info);
        rootGroup.setPadding(new Insets(12, 12, 12, 12));
        stage.setScene(new Scene(rootGroup));
        stage.show();
    }


    private void enableButton(final Button button) {
        button.setDisable(false);
    }

    private void disableButton(final Button button) {
        button.setDisable(true);
    }

}
