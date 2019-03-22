package ru.ifmo.se.s267880.lab56.csv;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

// TODO: separate CSV row Parser with CSV row with names Parser

/**
 * A simiple CSV reader.
 * @author Tran Quang Loc
 */
public class CsvReader {
    public class ParsingError extends RuntimeException {
        public ParsingError(String msg) {
            super(msg);
        }
    }

    private InputStream in;
    private List<String> header;

    private CsvLexerStateMachine lexer;
    private List<String> currentRow = new LinkedList<>();
    private boolean rowEnd;

    /**
     * Initialize the reader with a csv stream with no header.
     */
    public CsvReader(InputStream in) {
        this.in = in;
        header = null;
        lexer = new CsvLexerStateMachine() {
            @Override
            public void whenGotNewField(String field) {
                currentRow.add(field);
            }

            @Override
            public void whenRowEnd() {
                rowEnd = true;
            }
        };
    }

    /**
     * Initialize the reader. If withHeader is set, then the reader will parse the first row of the stream
     * and let it be the header.
     * @throws IOException
     */
    public CsvReader(InputStream in, boolean withHeader) throws IOException {
        this(in);
        if (withHeader) {
            this.header = getNextRow();
        }
    }

    /**
     * Get the next row and return its fields.
     * @return
     * @throws IOException
     */
    public List<String> getNextRow() throws IOException {
        currentRow.clear();
        rowEnd = false;
        while (!rowEnd) {
            if (!lexer.feed(in.read())) {
                return null;
            }
        }
        if (header != null && header.size() != currentRow.size()) {
            throw new ParsingError("The number of entries of header row and the current row are not equals");
        }
        return new LinkedList<>(currentRow);
    }

    public List<List<String>> getAllRows() throws IOException {
        LinkedList<List<String>> res = new LinkedList<>();
        do {
            res.add(getNextRow());
        } while (res.getLast() != null);
        res.removeLast();
        return res;
    }

    /**
     * Get the next row and return its fields corresponding to the header.
     * @return
     * @throws IOException
     */
    public Map<String, String> getNextRowWithNames() throws IOException {
        if (header == null) {
            throw new ParsingError("The current parsing process had no header.");
        }
        if (getNextRow() == null) return null;
        Map<String, String> res = new HashMap<>();
        for (Iterator<String> name = header.iterator(), value = currentRow.iterator();
             name.hasNext() && value.hasNext(); ) {
            res.put(name.next(), value.next());
        }

        return res;
    }

    public List<Map<String, String>> getAllRowsWithNames() throws IOException {
        LinkedList<Map<String, String>> res = new LinkedList<>();
        do {
            res.add(getNextRowWithNames());
        } while (res.getLast() != null);
        res.removeLast();
        return res;
    }

    /**
     * Get the header row's field.
     * @return null when the reader is initialized without header.
     */
    public List<String> getHeader() {
        return Collections.unmodifiableList(header);
    }
}
