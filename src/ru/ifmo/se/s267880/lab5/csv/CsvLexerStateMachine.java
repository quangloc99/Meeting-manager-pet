package ru.ifmo.se.s267880.lab5.csv;

import ru.ifmo.se.s267880.lab5.csv.CsvHelper;

// TODO: handle the EOF more properly
abstract public class CsvLexerStateMachine {
    protected interface State {
        State process(int nextByte);
    }

    protected class LexingError extends RuntimeException {}

    protected String currentField;
    protected State currentState;

    public CsvLexerStateMachine() {
        reset();
    }

    public void reset() {
        currentField = "";
        currentState = this::beginParse;
    }

    public boolean feed(int nextByte) {
        if (currentState == null) return false;
        currentState = currentState.process(nextByte);
        return true;
    }

    public abstract void whenGotNewField(String field);
    public abstract void whenRowEnd();


    protected State beginParse(int nextByte) { return beginParseField(nextByte); }
    protected State beginParseField(int nextByte) {
        currentField = "";
        if (nextByte == CsvHelper.QUOTE) return this::parseQuotedField;
        if (CsvHelper.isTextData(nextByte)) {
            currentField += (char) nextByte;
            return this::parseField;
        }
        return parseEndOfField(nextByte);
    }

    protected State parseField(int nextByte) {
        if (CsvHelper.isTextData(nextByte)) {
            currentField += (char) nextByte;
            return this::parseField;
        }
        return parseEndOfField(nextByte);
    }

    protected State parseLF(int nextByte) {
        if (nextByte != CsvHelper.LF) {
            throw new LexingError();
        }
        whenRowEnd();
        return this::beginParseField;
    }

    protected State parseQuotedField(int nextByte) {
        if (CsvHelper.isTextData(nextByte) || nextByte == CsvHelper.COMMA || nextByte == CsvHelper.CR || nextByte == CsvHelper.LF) {
            currentField += (char)nextByte;
            return this::parseQuotedField;
        }
        if (nextByte == CsvHelper.QUOTE) return this::tryParseDoubleQuote;
        throw new LexingError();
    }

    protected State tryParseDoubleQuote(int nextByte) {
        if (nextByte == CsvHelper.QUOTE) {
            currentField += (char) CsvHelper.QUOTE;
            return this::parseQuotedField;
        }
        return parseEndOfField(nextByte);
    }

    protected State parseEndOfField(int nextByte) {
        if (nextByte == -1 || nextByte == CsvHelper.COMMA || nextByte == CsvHelper.CR) {
            whenGotNewField(currentField);
        }
        if (nextByte == -1) {
            whenRowEnd();
            return null;
        }
        if (nextByte == CsvHelper.COMMA) {
            return this::beginParseField;
        }
        if (nextByte == CsvHelper.CR) {
            return this::parseLF;
        }
        throw new LexingError();
    }
}
