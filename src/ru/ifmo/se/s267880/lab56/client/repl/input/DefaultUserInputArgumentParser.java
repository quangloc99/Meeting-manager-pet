package ru.ifmo.se.s267880.lab56.client.repl.input;

import java.io.IOException;
import java.io.Reader;
import java.text.CharacterIterator;
import java.util.function.Consumer;

public class DefaultUserInputArgumentParser implements UserInputArgumentParser {

    /**
     * Check if <code>c</code> can be a beginning of a argument for this parser.
     * @param c the character to be tested.
     * @return true if c can be the first character of the argument for this parser.
     */
    @Override
    public boolean isArgumentBeginning(char c) {
        return !Character.isWhitespace(c);
    }

    /**
     * Start parsing the argument.
     * @param onCompleteParsing a callback, will be called when argument is successfully parsed.
     * @param onError           a callback. Will be called when there is an error during parsing.
     */
    @Override
    public void beginParse(Reader userInputReader, Consumer<Object> onCompleteParsing, Consumer<Exception> onError) {
        StringBuilder res = new StringBuilder();
        try {
            while (true) {
                int ch = userInputReader.read();
                if (Character.isWhitespace(ch)) break;
                res.append((char) ch);
            }
            onCompleteParsing.accept(res.toString());
        } catch (IOException e) {
            onError.accept(e);
        }
    }
}
