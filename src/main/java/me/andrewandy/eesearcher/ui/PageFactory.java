package me.andrewandy.eesearcher.ui;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import me.andrewandy.eesearcher.Authenticator;

public interface PageFactory {

    Parent newLoginPage(Authenticator authenticator, double width, double height, Insets insets, final Runnable onSuccess, final Runnable onCancel);

}
