package me.andrewandy.eesearcher.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import me.andrewandy.eesearcher.Authenticator;

public interface PageFactory {

    Node newLoginPage(Authenticator authenticator, double width, double height, Insets insets, final Runnable onSuccess, final Runnable onCancel);

}
