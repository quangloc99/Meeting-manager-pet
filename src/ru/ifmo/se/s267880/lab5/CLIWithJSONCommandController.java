package ru.ifmo.se.s267880.lab5;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.MalformedJsonException;
import sun.misc.Unsafe;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Command controller that work with command line interface and allow user to enter JSON object.
 *
 * @author Tran Quang Loc
 */
public class CLIWithJSONCommandController extends CommandController {
    /**
     * An interface that will contains the usage of the command to show to the user beside the handler itself.
     */
    public interface HandlerWithUsage extends CommandController.Handler {
        default String getUsage() {
            return "This command has no usage";
        }
    }

    /**
     * A method to remove <code>JsonParser.NON_EXECUTE_PREFIX</code>'s content.
     *
     * <p>This method is a hack.</p>
     *
     * <h6>The problem.</h6>
     * <p>
     *     My task asks me to implements some commands, and one of them has 2 type of argument: a json object and a number.
     *     We all know that a raw number is also a valid Json object, so all I need is just read the Json object.
     *     But Google is so smart: when reading a number with 1 digit, Gson tries to read 4 more in order to find out whether
     *     or not the Json string contains a string called
     *     <a href="https://google.github.io/gson/apidocs/com/google/gson/stream/JsonReader.html#nonexecuteprefix">
     *         "non execute prefix"
     *     </a> for security reason. So in order to execute the current command, the user need to enter 3 more character,
     *     (because they already hit enter, an '\n' is read).
     * </p>
     * <p>
     *     Of course the easiest solution for the above problem is to check if the first non-whitespace character is a
     *     number or is an open brace. But the second problem is that I want the command to be execute with more than
     *     one arguments. For example we want an command to insert an object into a specific place in a collection,
     *     so we need 2 arguments. And the problem now, is when using JsonReader or JsonParser, for the sake of the speed,
     *     the reader will try to read all the lines, into its buffer, so there is "almost" no way to get the next data
     *     in the same line after an argument is read, unless, using the JsonParser itself to read more.
     * </p>
     * <p>
     *     And also because of its fancy function, gson can read unquoted string, so I also use JsonParser as both of my
     *     reader: the command reader and the input reader.
     * </p>
     * <h6>The solution.</h6>
     * <p>
     *     Because this is a small project, and currently not related to the internet, so this method was made so it can
     *     remove the content of the "non execute prefix" in the JsonReader object. That way, the commands is more
     *     freely to read the user input, and JsonParser is still safe and not malfunction.
     * </p>
     * <p>To archive this, reflection is used with a little help of online code that suspend the looger (so it will look nicer).</p>
     *
     */
    public boolean removeGSONNonExecutablePrefix() {
        /*
         * The code for turning of the logger can be found here:
         * https://stackoverflow.com/questions/46454995/how-to-hide-warning-illegal-reflective-access-in-java-9-without-jvm-argument
         */
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            Unsafe u = (Unsafe) theUnsafe.get(null);

            Class cls = Class.forName("jdk.internal.module.IllegalAccessLogger");
            Field logger = cls.getDeclaredField("logger");
            u.putObjectVolatile(cls, u.staticFieldOffset(logger), null);
        } catch (Exception e) {
            return false;
        }

        try {
            Field nonExecutePrefixField = JsonReader.class.getDeclaredField("NON_EXECUTE_PREFIX");
            nonExecutePrefixField.setAccessible(true);

            Field modifier = Field.class.getDeclaredField("modifiers");
            modifier.setAccessible(true);

            modifier.set(nonExecutePrefixField,nonExecutePrefixField.getModifiers() & ~Modifier.FINAL);
            nonExecutePrefixField.set(null, new char[]{});
        } catch (IllegalAccessException e) {
            return false;
        } catch (NoSuchFieldException e) {
            return false;
        }
        return true;
    }

    private BufferedReader userInputStream;
    private JsonReader jreader;

    /**
     * Use this constructor for custom type of input. Maybe from file, or interactive with other problem. Who knows?
     * After initialized, "list-commands" command is added to show all the commands.
     * @param userInputStream the stream that receives the user's input.
     */
    public CLIWithJSONCommandController(InputStream userInputStream) {
        this.userInputStream = new BufferedReader(new InputStreamReader(userInputStream));
        addCommand("list-commands", "[Additional] List all the commands.", (Object[] args) -> {
            System.out.println("# Commands list:");
            commandHandlers.forEach((commandName, handler) -> {
                System.out.printf("- %s\n", commandName);
                if (!(handler instanceof HandlerWithUsage)) return;
                for (String s : ((HandlerWithUsage) handler).getUsage().split("\n")) {
                    System.out.printf("\t%s\n", s);
                }
                System.out.println();
            });
            return CommandController.SUCCESS;
        });
    }

    /**
     * Add a command with its usage. This function is oriented to use with lambda.
     * @param commandName the name of the command.
     * @param usage - the usage of the command. It can be line separated. The command "list-commands" will split it and display nicely.
     * @param handler - the handler for this command.
     */
    public void addCommand(String commandName, String usage, CommandController.Handler handler) {
        super.addCommand(commandName, new HandlerWithUsage() {
            @Override
            public int process(Object[] args) throws Exception {
                return handler.process(args);
            }

            @Override
            public String getUsage() {
                return usage;
            }
        });
    }

    /**
     * Get the user command from the userInputStream (passed into the {@link #CLIWithJSONCommandController(InputStream) constructor}).
     *
     * <p>Note: known-bug: because this method use google's </p>
     * @return the user command.
     */
    protected String getUserCommand() {
        try {
            jreader = new JsonReader(userInputStream);  // always initialize a new object, so it will read from the new line.
            jreader.setLenient(true);
            return jreader.nextString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the input as a json object. It also read number, string, boolean, since they are still valid json objects.
     * @return
     */
    @Override
    protected Object getUserInput() {
        if (jreader == null) {
            jreader = new JsonReader(userInputStream);
        }
        jreader.setLenient(true);
        return (new JsonParser()).parse(jreader);
    }
}
