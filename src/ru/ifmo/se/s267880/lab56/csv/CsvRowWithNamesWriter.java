package ru.ifmo.se.s267880.lab56.csv;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;

/**
 * A simple CSV writer with header.
 * @author Tran Quang Loc
 */
public class CsvRowWithNamesWriter extends CsvRowWriter implements Closeable, AutoCloseable {
    private List<String> header;

    /**
     * The header row will be written while initializing.
     * @param out the output stream that will be written.
     * @param header the header row.
     * @throws IOException
     */
    public CsvRowWithNamesWriter(OutputStream out, List<String> header) throws IOException {
        super(out, header.size());
        this.header = new LinkedList(header);
        super.writeRow(this.header);
    }

    /**
     * Write a row with fields that corresponding to the header.
     * Note that if there is no field corresponding to 1 of the header's fields, then it will be a empty string.
     * @throws IOException
     */
    public void writeRow(Map<String, String> rowWithNames) throws IOException {
        List<String> row = new LinkedList<>();
        for (String name: header) {
            String val = rowWithNames.get(name);
            if (val == null) {
                val = "";
            }
            row.add(val);
        }
        super.writeRow(row);
    }
}
