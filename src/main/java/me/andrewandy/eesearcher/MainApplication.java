package me.andrewandy.eesearcher;

import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.stage.Stage;
import me.andrewandy.eesearcher.module.BackendModule;
import me.andrewandy.eesearcher.module.FrontendModule;
import me.andrewandy.eesearcher.ui.GuestHomepage;
import me.andrewandy.eesearcher.ui.LegacyPicker;
import me.andrewandy.eesearcher.ui.LoginWindow;

import java.util.Locale;

public class MainApplication extends Application {

    private static String cliWindow;
    private static Thread HEART_BEAT;

    public static Thread getHeartBeat() {
        return HEART_BEAT;
    }

    public static boolean isPrimaryThread() {
        return Thread.currentThread() == HEART_BEAT;
    }

    public static void main(String[] args) {
        String window = "null";
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-w") && i + 1 < args.length) {
                window = args[i + 1];
                break;
            }
        }
        cliWindow = window;
        switch (window.toLowerCase(Locale.ENGLISH)) {
            case "loginwindow":
                LoginWindow.main(args);
                break;
            case "legacypicker":
                LegacyPicker.main(args);
                break;
            default:
                launch(args);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        if (HEART_BEAT != null) {
            HEART_BEAT = Thread.currentThread();
        }
        final Injector injector = Guice.createInjector(com.google.inject.Stage.PRODUCTION, new BackendModule(), new FrontendModule(primaryStage));
        final GuestHomepage homepage = injector.getInstance(GuestHomepage.class);
        homepage.draw();
    }

}
