package ru.ifmo.se.s267880.lab5;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class CommandController {
    public interface Handler {}
    public interface HandlerWithNoArgument extends Handler {
        void process();
    }

    public interface HandlerWithJson extends Handler {
        void process(JsonElement json);
    }

    public class CommandNotFoundException extends Exception {
        public CommandNotFoundException(String commandName) {
            super("Command \"" + commandName + "\" not found. Use command \"list-commands\" for the list of usable commands.");
        }
    }

    private Map<String, Handler> commandHandlers = new TreeMap<>();  // so the commands can be alphabetized
    private Map<String, String> commandUsages = new HashMap<>();

    protected InputStreamReader userInputStream;

    public CommandController(InputStream userInputStream) {
        this.userInputStream = new InputStreamReader(userInputStream);
        addCommand("list-commands", "list all the commands", () -> {
            System.out.println("Commands list:");
            commandHandlers.forEach((commandName, handler) -> System.out.printf("   - %15s - %s\n",
                commandName + (handler instanceof HandlerWithJson ? " {input}" : ""),
                commandUsages.get(commandName)
            ));
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

    public void prompt() throws IOException, CommandNotFoundException {
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

    protected JsonElement getUserJsonInput() {
        JsonReader jreader = new JsonReader(userInputStream);
        jreader.setLenient(true);
        return (new JsonParser()).parse(jreader);
    }
}
