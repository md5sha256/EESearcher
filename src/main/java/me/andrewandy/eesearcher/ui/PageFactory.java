package me.andrewandy.eesearcher.ui;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import me.andrewandy.eesearcher.Authenticator;
import org.jetbrains.annotations.NotNull;

public interface PageFactory {

    @NotNull Parent newLoginPage(@NotNull Authenticator authenticator,
                                 double width,
                                 double height,
                                 @NotNull Insets insets,
                                 @NotNull Runnable onSuccess,
                                 @NotNull Runnable onCancel);

}
