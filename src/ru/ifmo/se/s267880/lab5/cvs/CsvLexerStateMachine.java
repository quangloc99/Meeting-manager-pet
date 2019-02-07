package ru.ifmo.se.s267880.lab5.cvs;

abstract public class CsvLexerStateMachine {
    protected interface State {
        State process(int nextByte);
    }

    protected class LexingError extends RuntimeException {}

    private final int CR = '\r';
    private final int LF = '\n';
    private final int COMMA = ',';
    private final int QUOTE = '"';
    private boolean isTextData(int ch) {
        return ch == 0x20 || ch ==0x21 || (0x23 <= ch && ch <= 0x2B) || (0x2D <= ch && ch <= 0x7E);
    }

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
        currentState = currentState.process(nextByte);
        return currentState == null;
    }

    public abstract void whenGotNewField(String field);
    public abstract void whenRowEnd();


    protected State beginParse(int nextByte) { return beginParseField(nextByte); }
    protected State beginParseField(int nextByte) {
        currentField = "";
        if (nextByte == QUOTE) return this::parseQuotedField;
        if (isTextData(nextByte)) {
            currentField += (char) nextByte;
            return this::parseField;
        }
        return parseEndOfField(nextByte);
    }

    protected State parseField(int nextByte) {
        if (isTextData(nextByte)) {
            currentField += (char) nextByte;
            return this::parseField;
        }
        return parseEndOfField(nextByte);
    }

    protected State parseLF(int nextByte) {
        if (nextByte != LF) {
            throw new LexingError();
        }
        whenRowEnd();
        return this::beginParseField;
    }

    protected State parseQuotedField(int nextByte) {
        if (isTextData(nextByte) || nextByte == COMMA || nextByte == CR || nextByte == LF) {
            currentField += (char)nextByte;
            return this::parseQuotedField;
        }
        if (nextByte == QUOTE) return this::tryParseDoubleQuote;
        throw new LexingError();
    }

    protected State tryParseDoubleQuote(int nextByte) {
        if (nextByte == QUOTE) {
            currentField += (char) QUOTE;
            return this::parseQuotedField;
        }
        return parseEndOfField(nextByte);
    }

    protected State parseEndOfField(int nextByte) {
        if (nextByte == -1 || nextByte == COMMA || nextByte == CR) {
            whenGotNewField(currentField);
        }
        if (nextByte == -1) {
            return null;
        }
        if (nextByte == COMMA) {
            return this::beginParseField;
        }
        if (nextByte == CR) {
            return this::parseLF;
        }
        throw new LexingError();
    }
}
