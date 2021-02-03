package me.andrewandy.eesearcher.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class LoginWindow extends Application {

    private final PageFactory pageFactory = new PageFactoryImpl();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primary) {

        final StackPane root = new StackPane();
        final GridPane innerGrid = new GridPane();
        innerGrid.setPadding(new Insets(12, 12, 12, 12));
        innerGrid.setVgap(20);
        innerGrid.setHgap(20);
        innerGrid.setAlignment(Pos.CENTER);
        innerGrid.setMaxWidth(Double.MAX_VALUE);
        innerGrid.setMaxHeight(Double.MAX_VALUE);

        final Button buttonAdminLogin = new Button("Guest Login");
        final Pane pane = new Pane(buttonAdminLogin);
        pane.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        pane.setPadding(new Insets(12, 12, 12, 12));
        pane.setMaxWidth(Double.MAX_VALUE);
        pane.setMaxHeight(Double.MAX_VALUE);
        innerGrid.addRow(0, pane);
        final Node loginPage = pageFactory.newLoginPage((user, password) -> true, 200, 400, new Insets(0, 0, 0, 0),
                () -> {
                },
                () -> {
                }
        );
        innerGrid.add(loginPage, 0, 1);
        root.getChildren().add(innerGrid);
        primary.setTitle("Login Page");
        primary.setScene(new Scene(root, 720, 480));
        primary.show();
    }

}
