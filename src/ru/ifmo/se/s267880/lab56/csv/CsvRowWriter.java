package ru.ifmo.se.s267880.lab56.csv;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.LinkedList;

/**
 * A simple CSV writer. This writer does not have field. It just write row by row.
 * @author Tran Quang Loc
 */
public class CsvRowWriter implements Closeable, AutoCloseable {
    private OutputStream out;
    private int nRowFields;
    private boolean firstRowWritten = false;

    /**
     * @param nRowFields the number of fields, for safety while writing.
     */
    public CsvRowWriter(OutputStream out, int nRowFields) {
        assert(nRowFields > 0);
        this.nRowFields = nRowFields;
        this.out = out;
    }

    // TODO: (or not :)) optimization for memory.

    /**
     * Write a row to the stream.
     * @throws IOException
     * @throws AssertionError when the row's size is not equals to {@link #nRowFields}.
     */
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

    /**
     * Close the stream.
     * @throws IOException
     */
    public void close() throws IOException {
        out.close();
    }

    public int getNRowFields() {
        return nRowFields;
    }
}
