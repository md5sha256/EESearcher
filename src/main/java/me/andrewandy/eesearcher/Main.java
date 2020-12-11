package me.andrewandy.eesearcher;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
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
    }

}
