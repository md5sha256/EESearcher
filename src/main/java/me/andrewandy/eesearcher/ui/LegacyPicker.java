package me.andrewandy.eesearcher.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.util.List;

public class LegacyPicker extends Application {

    private final Desktop desktop = Desktop.getDesktop();

    private File[] files = new File[0];

    public static void main(String[] args) {
        launch(args);
    }

    private static void configureFileChooser(final FileChooser fileChooser) {
        fileChooser.setTitle("View Pictures");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
    }

    public File[] getPickedFiles() {
        return files;
    }

    @Override
    public void start(final Stage stage) {
        stage.setTitle("Import Extended Essays");

        final FileChooser fileChooser = new FileChooser();
        final Button selectMultipleButton = new Button("Select Extended Essays...");
        selectMultipleButton.setTextFill(Color.BLACK);
        final Button clearSelection = new Button("Clear Selection");
        clearSelection.setTextFill(Color.BLACK);
        // Make sure the buttons are equally spaced.
        clearSelection.setLayoutX(selectMultipleButton.getLayoutX());
        final Label status = new Label(" ");
        clearSelection.setOnAction(event -> {
            this.files = new File[0];
            status.setTextFill(Color.GREEN);
            status.setText("Selection cleared!");
        });
        final Label info = new Label("Select files by dragging and dropping or importing.");
        info.setTextFill(Color.BLACK);

        selectMultipleButton.setOnAction(e -> {
            configureFileChooser(fileChooser);
            final List<File> files = fileChooser.showOpenMultipleDialog(stage);
            if (files != null) {
                this.files = files.toArray(new File[0]);
                status.setTextFill(Color.GREEN);
                status.setText(String.format("%d Files added!", files.size()));
            } else {
                status.setTextFill(Color.BLACK);
                status.setText("No Files Added");
            }
        });


        final GridPane inputGridPane = new GridPane();
        GridPane.setConstraints(selectMultipleButton, 0, 1);
        GridPane.setConstraints(clearSelection, 1, 1);
        inputGridPane.setHgap(6);
        inputGridPane.setVgap(6);
        inputGridPane.getChildren().addAll(selectMultipleButton, clearSelection);

        final Pane rootGroup = new VBox(12);
        rootGroup.getChildren().addAll(info, status, inputGridPane);
        rootGroup.setPadding(new Insets(12, 12, 12, 12));

        stage.setScene(new Scene(rootGroup));
        stage.show();
    }

}
