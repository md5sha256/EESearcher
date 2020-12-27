package me.andrewandy.eesearcher;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class FileImporter extends Application {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Label label = new Label("Drag a file to me.");
        Label dropped = new Label("");
        VBox dragTarget = new VBox();
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
            final List<File> files = dragboard.getFiles();
            boolean valid = !files.removeIf(file -> !Utils.isValidPDF(file));
            if (valid) {
                dropped.setText("PDF(s) Valid!");
            } else {
                dropped.setText("Invalid PDF Found!");
            }
            event.setDropCompleted(valid);
            event.consume();
        });


        StackPane root = new StackPane();
        root.getChildren().add(dragTarget);

        Scene scene = new Scene(root, 500, 250);

        primaryStage.setTitle("Drag Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


}
