package me.andrewandy.eesearcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

public class Utils {

    public static boolean isValidPDF(final File file) {
        try (final Scanner scanner = new Scanner(new FileInputStream(file))) {
            final String s = scanner.nextLine();
            return s.startsWith("%PDF");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static void isValidPDF(final Path path) {
        isValidPDF(path.toFile());
    }

}
