package me.andrewandy.eesearcher;

import me.andrewandy.eesearcher.ui.FileImporter;
import me.andrewandy.eesearcher.ui.LegacyPicker;
import me.andrewandy.eesearcher.ui.LoginWindow;
import me.andrewandy.eesearcher.ui.Picker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    private static final String
            TITLE = "(?<=(Title|Topic): )(\\w+.*(\\s+|\\w+)).*",
            RQ = "(?<=(Research Question|RQ)): (\\w+.*(\\s+|\\w+)).*",
            SUBJECT = "(^Subject:\\s+(\\w+)$)";
    private static Thread HEART_BEAT;

    public static Thread getHeartBeat() {
        return HEART_BEAT;
    }

    public static void main(String[] args) {
        if (HEART_BEAT != null) {
            HEART_BEAT = Thread.currentThread();
        }
        String window = "null";
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-w") && i + 1 < args.length) {
                window = args[i + 1];
                break;
            }
        }
        switch (window.toLowerCase(Locale.ENGLISH)) {
            case "loginwindow" -> LoginWindow.main(args);
            case "picker" -> Picker.main(args);
            case "legacypicker" -> LegacyPicker.main(args);
            case "fileimporter" -> FileImporter.main(args);
            default -> System.out.printf("Unknown arg 'window': %s", window);
        }
    }

    private static void testTitles() {
        final Scanner scanner = new Scanner(System.in);
        Pattern pattern = Pattern.compile("(?<=(Title|Topic): )(\\w+.*(\\s+|\\w+)).*", Pattern.CASE_INSENSITIVE);
        while (true) {
            System.out.println("input a title");
            final String in = scanner.nextLine();
            if (in.equalsIgnoreCase("exit")) {
                break;
            }
            final Matcher matcher = pattern.matcher(in);
            if (matcher.find()) {
                final String title = matcher.group(2);
                System.out.println("Parsed title: " + title);
            }
        }
        scanner.close();
    }

    private static void testSubjects() {
        final Scanner scanner = new Scanner(System.in);
        Pattern SUBJECT_PARSER = Pattern.compile("(^Subject:\\s+(\\w+)$)", Pattern.CASE_INSENSITIVE);
        while (true) {
            System.out.println("input a subject");
            final String in = scanner.nextLine();
            if (in.equalsIgnoreCase("exit")) {
                break;
            }
            final Matcher matcher = SUBJECT_PARSER.matcher(in);
            if (matcher.find()) {
                System.out.println("Found: " + matcher.group(2));
            } else {
                System.out.println("None found.");
            }
        }
        scanner.close();
    }

    private void parseText() {
        System.out.println("--- Start ---");

        final File file = new File("ee.pdf");
        if (!file.exists()) {
            System.out.println("No file found!");
            return;
        }
        final List<String> textByPage;
        long temp = System.currentTimeMillis();
        System.out.println("Loading pdf into memory; parsing text...");
        try {
            textByPage = Parser.parseTextByPage(Parser.parseDocument(new FileInputStream(file)));
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Failed to parse text from source.");
            return;
        }
        System.out.println("Parsed text in " + (System.currentTimeMillis() - temp) + "ms");
        System.out.println("Parsing keywords...");
        temp = System.currentTimeMillis();
        final KeywordFinder finder = new KeywordFinder();
        textByPage.forEach((s) -> finder.count(s, false));
        final Map<String, Integer> map = finder.getResults();
        final List<String> processed = finder.getProcessedResults();
        for (String s : processed) {
            System.out.println("Word: " + s + " | Number of Ocurrences: " + map.get(s));
        }
        System.out.println("Parsed keywords in " + (System.currentTimeMillis() - temp) + "ms");
        System.out.println("--- End ---");
    }

}
