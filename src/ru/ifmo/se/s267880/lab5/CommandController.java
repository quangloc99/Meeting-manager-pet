package ru.ifmo.se.s267880.lab5;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class CommandController {
    public interface Handler {}
    public interface HandlerWithNoArgument extends Handler {
        void process() throws Exception ;
    }

    public interface HandlerWithJson extends Handler {
        void process(JsonElement json) throws Exception;
    }

    public class CommandNotFoundException extends Exception {
        public CommandNotFoundException(String commandName) {
            super("Command \"" + commandName + "\" not found. Use command \"list-commands\" for the list of usable commands.");
        }
    }

    private Map<String, Handler> commandHandlers = new TreeMap<>();  // so the commands can be alphabetized
    private Map<String, String> commandUsages = new HashMap<>();

    protected BufferedReader userInputStream;

    public CommandController(InputStream userInputStream) {
        this.userInputStream = new BufferedReader(new InputStreamReader(userInputStream));
        addCommand("list-commands", "[Additional] List all the commands.", () -> {
            System.out.println("# Commands list:");
            commandHandlers.forEach((commandName, handler) -> {
                System.out.printf("- %s %s\n", commandName, (handler instanceof HandlerWithJson ? "{arg}" : ""));
                for (String s : commandUsages.get(commandName).split("\n")) {
                    System.out.printf("\t%s\n", s);
                }
                System.out.println();
            });
        });
    }

    public CommandController() {
        this(System.in);
    }

    public void addCommand(String commandName, String usage, HandlerWithNoArgument handler) {
        commandUsages.put(commandName, usage);
        commandHandlers.put(commandName, handler);
    }

    public void addCommandWithJson(String commandName, String usage, HandlerWithJson handler) {
        commandUsages.put(commandName, usage);
        commandHandlers.put(commandName, handler);
    }

    public Handler getCommandHandler(String commandName) {
        return commandHandlers.get(commandName);
    }

    public String getCommandUsage(String commandName) {
        return commandUsages.get(commandName);
    }

    public void prompt() throws IOException, CommandNotFoundException, Exception {
        System.out.printf("> ");
        String userCommand = getUserCommand();
        if (!commandHandlers.containsKey(userCommand)) {
            throw new CommandNotFoundException(userCommand);
        }
        Handler handler = commandHandlers.get(userCommand);
        if (handler instanceof HandlerWithJson) {
            ((HandlerWithJson)handler).process(getUserJsonInput());
        } else {
            ((HandlerWithNoArgument)handler).process();
        }
    }

    protected String getUserCommand() throws IOException {
        int currentByte;
        while ((currentByte = userInputStream.read()) == ' ');
        String command = "" + (char) currentByte;
        while (true) {
            currentByte = userInputStream.read();
            if (Character.isWhitespace(currentByte) || currentByte == -1) break;
            command += (char) currentByte;
        }
        return command;
    }

    protected JsonElement getUserJsonInput() throws IOException {
        JsonReader jreader = new JsonReader(userInputStream);

        // try to preread some primitive type because after reading them the parser will call the "hasNext" method,
        // and the reader will try to read more, so the command will not be executed immediately.
        if (jreader.peek() == JsonToken.NUMBER)
            return new JsonPrimitive(jreader.nextLong());
        else if (jreader.peek() == JsonToken.STRING)
            return new JsonPrimitive(jreader.nextString());
        else if (jreader.peek() == JsonToken.BOOLEAN)
            return new JsonPrimitive(jreader.nextBoolean());

        jreader.setLenient(false);
        return (new JsonParser()).parse(jreader);
    }
}
