package ru.ifmo.se.s267880.lab5.csv;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.LinkedList;

public class CsvRowWriter {
    private OutputStream out;
    private int nRowFields;
    private boolean firstRowWritten = false;

    public CsvRowWriter(OutputStream out, int nRowFields) {
        assert(nRowFields > 0);
        this.nRowFields = nRowFields;
        this.out = out;
    }

    // TODO: (or not :)) optimization for memory.
    public void writeRow(List<String> row) throws IOException {
        assert(row.size() == nRowFields);
        if (firstRowWritten) {
            out.write(CsvHelper.CR);
            out.write(CsvHelper.LF);
        }

        List<String> transformedRow = new LinkedList<>();
        for (String s: row) {
            transformedRow.add(CsvHelper.encloseQuote(s));
        }
        out.write(String.join(",", transformedRow).getBytes());
        firstRowWritten = true;
    }

    public void close() throws IOException {
        out.close();
    }

    public int getNRowFields() {
        return nRowFields;
    }
}
