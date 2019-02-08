package ru.ifmo.se.s267880.lab5.csv;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

// TODO: separate CSV row Parser with CSV row with names Parser
public class CsvParser {
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

    public CsvParser(InputStream in) {
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

    public CsvParser(InputStream in, boolean withHeader) throws IOException {
        this(in);
        if (withHeader) {
            this.header = getNextRow();
        }
    }

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
        return List.copyOf(currentRow);
    }

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
}
