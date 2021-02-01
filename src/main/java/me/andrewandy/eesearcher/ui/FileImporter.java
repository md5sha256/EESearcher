package me.andrewandy.eesearcher.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import me.andrewandy.eesearcher.Utils;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

public class FileImporter extends Application {


    private final Collection<File> selectedFiles = new HashSet<>();

    private static FileChooser newChooser() {
        final FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        chooser.setTitle("PDF Files");
        return chooser;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    public Collection<File> getSelectedFiles() {
        return new HashSet<>(selectedFiles);
    }

    @Override
    public void start(Stage primaryStage) {

        final Button selectButton = new Button("Select PDFs");
        Label label = new Label("Select files by dragging and dropping or importing.");

        selectButton.setOnAction(event -> selectedFiles.addAll(newChooser().showOpenMultipleDialog(primaryStage)));

        Label dropped = new Label("");
        VBox dragTarget = new VBox(12);
        dragTarget.getChildren().addAll(label, dropped);
        dragTarget.setOnDragOver(event -> {
            if (event.getGestureSource() != dragTarget && event.getDragboard().hasFiles()) {
                /* allow for both copying and moving, whatever user chooses */
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            dropped.setText("");
            event.consume();
        });

        dragTarget.setOnMouseClicked(event -> {
            final MouseButton button = event.getButton();
            if (button == MouseButton.MIDDLE) {
                primaryStage.close();
            }
        });

        dragTarget.setOnDragDropped(event -> {
            final Dragboard dragboard = event.getDragboard();
            if (!dragboard.hasFiles()) {
                dropped.setText("Please drag a file!");
                event.consume();
                return;
            }
            boolean valid = !dragboard.getFiles().removeIf(file -> !Utils.isValidPDF(file));
            if (valid) {
                dropped.setText("PDF(s) Valid!");
                selectedFiles.addAll(dragboard.getFiles());
            } else {
                dropped.setText("Invalid PDF Found!");
            }
            event.setDropCompleted(valid);
            event.consume();
        });


        final GridPane inputGridPane = new GridPane();

        GridPane.setConstraints(selectButton, 0, 1);
        inputGridPane.setHgap(6);
        inputGridPane.setVgap(6);
        inputGridPane.getChildren().addAll(selectButton);
        dragTarget.getChildren().addAll(inputGridPane);
        dragTarget.setPadding(new Insets(12, 12, 12, 12));

        Scene scene = new Scene(dragTarget);

        primaryStage.setTitle("Drag Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


}
