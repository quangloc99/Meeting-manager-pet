package ru.ifmo.se.s267880.lab56.csv;

// TODO: handle the EOF more properly

/**
 * A lexer which is based on Finiate State Machine model. See this <a href="https://hackernoon.com/lexical-analysis-861b8bfe4cb0">tutorial </a>
 * for better understanding.
 *
 * The different here is I heavily using lambda instead of String for State. The benefit is name checking during compile time
 * and no "switch case" or "if else" hell.
 *
 * @author Tran Quang Loc
 */
abstract public class CsvLexerStateMachine {
    /**
     * The interface describe the lambda for each state.
     */
    protected interface State {
        /**
         * Process the current state with the given character.
         * @param nextByte the byte the represent the character, or -1 if EOF is reached.
         * @return the next state.
         */
        State process(int nextByte);
    }

    protected class LexingError extends RuntimeException {}

    /**
     * The current field name while parsing. It was left protected for inheritance, in case when someone when more states.
     */
    protected String currentField;

    /**
     * The current state of the machine.
     */
    protected State currentState;

    public CsvLexerStateMachine() {
        reset();
    }

    /**
     * Reset the state and the currentField into the initial state.
     */
    public void reset() {
        currentField = "";
        currentState = this::beginParse;
    }

    /**
     * Put a character for the machine to run.
     * @return
     */
    public boolean feed(int nextByte) {
        if (currentState == null) return false;
        currentState = currentState.process(nextByte);
        return true;
    }

    /**
     * This method will be called when the lexer found a new field.
     */
    public abstract void whenGotNewField(String field);

    /**
     * This method will be called when the lexer found that the current position is an EOL.
     */
    public abstract void whenRowEnd();


    /**
     * The very first state of the machine. This state is just {@link #beginParseField(int)} state.
     * @return
     */

    protected State beginParse(int nextByte) { return beginParseField(nextByte); }

    /**
     * The state when begin parsing a field.
     * The next state will be {@link #parseQuotedField(int)}, {@link #parseField(int)} or the next state of
     * {@link #parseEndOfField(int)} (it will be called with the current input).
     * @return
     */
    protected State beginParseField(int nextByte) {
        currentField = "";
        if (nextByte == CsvHelper.QUOTE) return this::parseQuotedField;
        if (CsvHelper.isTextData(nextByte)) {
            currentField += (char) nextByte;
            return this::parseField;
        }
        return parseEndOfField(nextByte);
    }

    /**
     * This state will be repeated (will return itself) while parsing a field.
     * When done parsing field, {@link #parseEndOfField(int)} is called with the current input.
     * @return
     */
    protected State parseField(int nextByte) {
        if (CsvHelper.isTextData(nextByte)) {
            currentField += (char) nextByte;
            return this::parseField;
        }
        return parseEndOfField(nextByte);
    }

    /**
     * It is written in the <a href="https://tools.ietf.org/html/rfc4180#page-2.">specification</a>, that the EOL
     * is CRLF, so if in the previous state, we got CR, then now we must parse LF.
     * If there is no LF, then LexingError will be throws.
     * The next State after this is {@link #beginParseField(int)}
     * @throws LexingError
     * @return
     */
    protected State parseLF(int nextByte) {
        if (nextByte != CsvHelper.LF) {
            throw new LexingError();
        }
        whenRowEnd();
        return this::beginParseField;
    }

    /**
     * This one will return itself while parsing a quoted field.
     * If the current character is a quote, then it will jump to {@link #tryParseDoubleQuote(int)}, and after that
     * jump back here is there is a quote in the string.
     * @return
     */
    protected State parseQuotedField(int nextByte) {
        if (CsvHelper.isTextData(nextByte) || nextByte == CsvHelper.COMMA || nextByte == CsvHelper.CR || nextByte == CsvHelper.LF) {
            currentField += (char)nextByte;
            return this::parseQuotedField;
        }
        if (nextByte == CsvHelper.QUOTE) return this::tryParseDoubleQuote;
        throw new LexingError();
    }

    /**
     * This state will be called when the previous state is {@link #parseQuotedField(int)} and we got a quote.
     * If the current character is a quote, then we go back and continue parsing the quoted field.
     * Other wise we jump to parseEndOfField (with the current character) because this quote closed the field.
     * @return
     */
    protected State tryParseDoubleQuote(int nextByte) {
        if (nextByte == CsvHelper.QUOTE) {
            currentField += (char) CsvHelper.QUOTE;
            return this::parseQuotedField;
        }
        return parseEndOfField(nextByte);
    }

    /**
     * This state determine whether we got a new field or a new row, then it will invoke the events methods.
     * Then it will go to {@link #beginParseField(int)} or {@link #parseLF(int)} base on the current character.
     * @return
     */
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
