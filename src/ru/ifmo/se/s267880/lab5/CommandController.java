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

/**
 * A class for reading user input (both command and arguments) and execute the correspond command.
 * It look like a Commands manager, but the commands's implementations will be independent to this class.
 * Because of the scope of this lab, and the limitation of Google's gson library (and other's too, because they did not designed
 * to read input from stdin), this class only manages commands with the number of arguments not more than 1. But still
 * this class can be extends more.
 * @author Tran Quang Loc
 */
public class CommandController {
    /**
     * The base interface for Handler
     */
    public interface Handler {}

    /**
     * The interface that describes the handler lambda with no arguments.
     */
    public interface HandlerWithNoArgument extends Handler {
        void process() throws Exception ;
    }

    /**
     * The interface that describes the handler lambda with 1 arguments.
     */
    public interface HandlerWithJson extends Handler {
        void process(JsonElement json) throws Exception;
    }

    public class CommandNotFoundException extends Exception {
        public CommandNotFoundException(String commandName) {
            super("Usage \"" + commandName + "\" not found. Use command \"list-commands\" for the list of usable commands.");
        }
    }

    private Map<String, Handler> commandHandlers = new TreeMap<>();  // so the commands can be alphabetized
    private Map<String, String> commandUsages = new HashMap<>();

    private BufferedReader userInputStream;

    /**
     * Use this constructor for custom type of input. Maybe from file, or interactive with other problem. Who knows?
     * After initialized, "list-commands" command is added to show all the commands.
     * @param userInputStream the stream that receives the user's input.
     */
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

    /**
     * Initialize the Controller with stdin.
     * After initialized, "list-commands" command is added to show all the commands.
     */
    public CommandController() {
        this(System.in);
    }

    /**
     * Add a command with no argument.
     * @param commandName the name of the command
     * @param usage the usage of the command. It may contains multiple lines, separated by \n. "list-commands" command
     *              will print these lines nicely.
     * @param handler
     */
    public void addCommand(String commandName, String usage, HandlerWithNoArgument handler) {
        commandUsages.put(commandName, usage);
        commandHandlers.put(commandName, handler);
    }

    /**
     * Add a command with 1 argument - the JsonElement.
     * @param commandName the name of the command
     * @param usage the usage of the command. It may contains multiple lines, separated by \n. "list-commands" command
     *              will print these lines nicely.
     * @param handler
     */
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

    /**
     * Read user input and the execute the correspond command.
     * @throws IOException
     * @throws CommandNotFoundException
     * @throws Exception
     */
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

    /**
     * Get user command.
     * This function tried not to ead all the character from {@link #userInputStream}, so the {@link JsonParser} can
     * continue parsing from the next character.
     * @return
     * @throws IOException
     */
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

    /**
     * Ger user json input, or the json argument for a command.
     * @return
     * @throws IOException
     */
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
