package me.andrewandy.eesearcher;

import java.util.Scanner;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    private static final String
            TITLE = "(?<=(Title|Topic): )(\\w+.*(\\s+|\\w+)).*",
            RQ = "(?<=(Research Question|RQ)): (\\w+.*(\\s+|\\w+)).*",
            SUBJECT = "(^Subject:\\s+(\\w+)$)";


    public static void main(String[] args) {
        testTitles();
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

}
