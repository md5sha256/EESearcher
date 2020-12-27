package me.andrewandy.eesearcher;

import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.exception.SyntaxValidationException;
import org.apache.pdfbox.preflight.parser.PreflightParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class Utils {

    public static boolean isValidPDF(final File file) {
        ValidationResult result = null;
        try {
            PreflightParser parser = new PreflightParser(file);
            /* Parse the PDF file with PreflightParser that inherits from the NonSequentialParser.
             * Some additional controls are present to check a set of PDF/A requirements.
             * (Stream length consistency, EOL after some Keyword...)
             */
            parser.parse();

            /* Once the syntax validation is done,
             * the parser can provide a PreflightDocument
             * (that inherits from PDDocument)
             * This document process the end of PDF/A validation.
             */
            try (PreflightDocument document = parser.getPreflightDocument()) {
                document.validate();
                // Get validation result
                result = document.getResult();
            }

        } catch (IOException ex) {
            /* the parse method can throw a SyntaxValidationException
             * if the PDF file can't be parsed.
             * In this case, the exception contains an instance of ValidationResult
             */
            if (ex instanceof SyntaxValidationException) {
                result = ((SyntaxValidationException) ex).getResult();
            }
        }
        return result != null && result.isValid();
    }

    public static void isValidPDF(final Path path) {
        isValidPDF(path.toFile());
    }

}
