package ru.ifmo.se.s267880.lab56.client.repl.input;

import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.Reader;
import java.util.function.Consumer;

public class JsonUserInputArgumentParser implements UserInputArgumentParser {
    /**
     * Check if <code>c</code> can be a beginning of a argument for this parser.
     *
     * @param c the character to be tested.
     * @return true if c can be the first character of the argument for this parser.
     */
    @Override
    public boolean isArgumentBeginning(char c) {
        return c == '{' || c == '[' || c == '"';
    }

    /**
     * Start parsing the argument.
     *
     * @param onCompleteParsing a callback, will be called when argument is successfully parsed.
     * @param onError           a callback. Will be called when there is an error during parsing.
     */
    @Override
    public void beginParse(Reader userInputReader, Consumer<Object> onCompleteParsing, Consumer<Exception> onError) {
        try {
            JsonReader reader = new JsonReader(userInputReader);
            reader.setLenient(true);
            onCompleteParsing.accept(new JsonParser().parse(reader));
        } catch (Exception e) {
            onError.accept(e);
        }
    }
}
