package me.andrewandy.eesearcher;

import org.apache.pdfbox.cos.*;
import org.apache.pdfbox.multipdf.PDFCloneUtility;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    private static final Pattern SUBJECT_PARSER = Pattern.compile("(^Subject:\\s(\\w+)$)", Pattern.CASE_INSENSITIVE);


    public Parser() {

    }
    /*
    public void test() {
        final PDFParser parser;
        final PDDocument document;
        COSDocument document1 = document.getDocument();
        Collection<COSObject> objects = document1.getObjectsByType(COSName.ACTUAL_TEXT);
        COSObject object;
        final COSBase base;
        COSString string;
        string.getString()
    }

     */

    public void test1() {
        final String rawText = "Subject: Math";
        final Matcher matcher = SUBJECT_PARSER.matcher(rawText);
        if (matcher.find()) {
            final String subjectName = matcher.group(2);
        }
    }

}
