package ru.ifmo.se.s267880.lab56.client.repl.input;

import ru.ifmo.se.s267880.lab56.shared.functional.HandlerCallback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class UserInputProvider {
    private BufferedReader userInputReader;
    private List<UserInputArgumentParser> argumentParsers;
    private CharacterIterator currentLineIterator = null;
    private List<Object> args;
    private final Lock processingLock = new ReentrantLock();
    private HandlerCallback<List<Object>> getInputCallback;

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

    public void getInput(HandlerCallback<List<Object>> getInputCallback) {
        processingLock.lock();
        this.getInputCallback = getInputCallback;
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
        onComplete();
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

    private void onComplete() {
        getInputCallback.onSuccess(args);
        processingLock.unlock();
    }
    private void onError(Exception e) {
        getInputCallback.onError(e);
        processingLock.unlock();
    }
}
