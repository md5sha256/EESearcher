package me.andrewandy.eesearcher;

import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Parser {

    private static final Pattern SUBJECT_PARSER = Pattern.compile("(^Subject:\\s(\\w+)$)", Pattern.CASE_INSENSITIVE);

    private Parser parser;

    public static PDFParser parseDocument(final InputStream inputStream) throws IOException {
        final PDFParser parser = new PDFParser(new RandomAccessBuffer(inputStream));
        parser.parse();
        return parser;
    }

    public static String parseText(final InputStream inputStream) throws IOException {
        return parseText(parseDocument(inputStream));
    }

    public static String parseText(final PDFParser parser) throws IOException {
        final PDFTextStripper pdfStripper = new PDFTextStripper();
        try (final PDDocument document = parser.getPDDocument()){
            return pdfStripper.getText(document);
        }
    }

    public static List<String> parseTextByPage(final PDFParser parser) throws IOException {
        try (final PDDocument document = parser.getPDDocument()){
            final PDFTextStripper stripper = new PDFTextStripper();
            final List<String> pages = new ArrayList<>(document.getNumberOfPages());
            for (int i = 1; i < document.getNumberOfPages();) {
                stripper.setStartPage(i++);
                stripper.setEndPage(i);
                pages.add(stripper.getText(document));
            }
            return pages;
        }
    }

}
