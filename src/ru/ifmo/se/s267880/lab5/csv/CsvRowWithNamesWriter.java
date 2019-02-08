package ru.ifmo.se.s267880.lab5.csv;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;

public class CsvRowWithNamesWriter extends CsvRowWriter {
    private List<String> header;
    public CsvRowWithNamesWriter(OutputStream out, List<String> header) throws IOException {
        super(out, header.size());
        this.header = List.copyOf(header);
        super.writeRow(this.header);
    }

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
