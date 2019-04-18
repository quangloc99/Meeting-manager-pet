package ru.ifmo.se.s267880.lab56.client.repl.input;

import ru.ifmo.se.s267880.lab56.client.repl.input.UserInputArgumentParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class UserInputProvider {
    private BufferedReader userInputReader;
    private List<UserInputArgumentParser> argumentParsers;
    private CharacterIterator currentLineIterator = null;
    private List<Object> args;

    public UserInputProvider(Reader userInputReader, UserInputArgumentParser ... userInputProviders) {
        this.userInputReader = new BufferedReader(userInputReader);
        this.argumentParsers = Arrays.asList(userInputProviders);
    }

    private Reader oneByOneUserInputReader = new Reader() {
        @Override
        public int read(char[] chars, int offset, int length) throws IOException {
            if (length == 0) return 0;
            chars[offset] = pollCharacter();
            return 1;
        }

        @Override
        public void close() throws IOException { }
    };

    public void getInput() {
        try {
            getNewLine();
            args = new LinkedList<>();
            gettingInputLoop();
        } catch (IOException e) { onError(e); }
    }

    private void onCompleteParsingArgument(Object o) {
        args.add(o);
        gettingInputLoop();
    }

    private void gettingInputLoop() {
        for (; currentLineIterator.current() != CharacterIterator.DONE; currentLineIterator.next()) {
            for (UserInputArgumentParser argumentParser : argumentParsers) {
                if (!argumentParser.isArgumentBeginning(currentLineIterator.current()))  continue;
                argumentParser.beginParse(
                        oneByOneUserInputReader,
                        this::onCompleteParsingArgument,
                        this::onError
                );
                return ;
            }
        }
        onComplete(args);
    }

    private char pollCharacter() throws IOException {
        while (currentLineIterator == null || currentLineIterator.current() == CharacterIterator.DONE) {
            getNewLine();
        }
        char res = currentLineIterator.current();
        currentLineIterator.next();
        return res;
    }

    private void getNewLine() throws IOException {
        currentLineIterator = new StringCharacterIterator(userInputReader.readLine() + '\n');
    }

    public void onComplete(List<Object> args) {}
    public void onError(Exception e) {}
}
