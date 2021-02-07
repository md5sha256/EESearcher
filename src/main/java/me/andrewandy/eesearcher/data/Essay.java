package me.andrewandy.eesearcher.data;

import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.IOException;

public class Essay implements AutoCloseable {

    private final IndexData indexData;
    private final PDDocument document;

    public Essay(final IndexData indexData, final byte[] rawPDF) {
        this.indexData = indexData;
        try {
            PDFParser parser = new PDFParser(new RandomAccessBuffer(rawPDF));
            parser.parse();
            this.document = parser.getPDDocument();
        } catch (IOException ex) {
            // Should never happen!
            // FIXME log error
            throw new RuntimeException(ex);
        }
    }

    public Essay(final IndexData indexData, final PDDocument document) {
        this.indexData = indexData;
        this.document = document;
    }

    public IndexData getIndexData() {
        return indexData;
    }

    public PDDocument getDocument() {
        return document;
    }

    @Override
    public void close() throws IOException {
        document.close();
    }
}
