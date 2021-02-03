package me.andrewandy.eesearcher.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import me.andrewandy.eesearcher.Authenticator;
import org.jetbrains.annotations.NotNull;

public class PageFactoryImpl implements PageFactory {

    public static Border DEF_BORDER = new Border(new BorderStroke(Color.BLACK,
            BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));

    public @NotNull Parent newLoginPage(@NotNull Authenticator authenticator, double width, double height, @NotNull Insets insets, final @NotNull Runnable onSuccess, final @NotNull Runnable onCancel) {
        final GridPane root = new GridPane();
        root.setPadding(insets);
        root.setPrefSize(width, height);
        root.setVgap(10);

        final Label status = new Label(" ");

        final TextField usernameInput = new TextField();
        usernameInput.setPromptText("username");
        final PasswordField passwordInput = new PasswordField();
        passwordInput.setPromptText("password");

        usernameInput.setAlignment(Pos.CENTER_LEFT);
        passwordInput.setAlignment(Pos.CENTER_LEFT);
        passwordInput.setPadding(new Insets(10, 10, 10, 10));

        final GridPane buttonPane = new GridPane();
        buttonPane.setPadding(new Insets(10, 0, 10, 0));
        buttonPane.setHgap(10);
        buttonPane.setMaxWidth(width);

        final Button buttonLogin = new Button("Login");
        final Button buttonCancel = new Button("Cancel");

        buttonPane.add(buttonLogin, 0, 0);
        buttonPane.add(buttonCancel, 1, 0);

        buttonLogin.setMaxWidth(width);
        buttonCancel.setMaxWidth(width);

        buttonLogin.setOnMouseClicked(event -> {
            if (authenticator.tryAuth(usernameInput.getText(), passwordInput.getText())) {
                onSuccess.run();
            } else {
                status.setTextFill(Color.RED);
                status.setText("Invalid username or password.");
                passwordInput.clear();
            }
            event.consume();
        });

        buttonCancel.setOnMouseClicked(event -> {
            onCancel.run();
            event.consume();
        });

        root.addColumn(0, usernameInput, passwordInput, buttonPane);

        return root;
    }

}
