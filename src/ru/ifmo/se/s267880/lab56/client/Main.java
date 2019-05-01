package ru.ifmo.se.s267880.lab56.client;

import com.google.gson.stream.JsonReader;
import ru.ifmo.se.s267880.lab56.client.commandHandlers.*;
import ru.ifmo.se.s267880.lab56.client.repl.MainLoop;
import ru.ifmo.se.s267880.lab56.client.repl.input.DefaultUserInputArgumentParser;
import ru.ifmo.se.s267880.lab56.client.repl.input.JsonUserInputArgumentParser;
import ru.ifmo.se.s267880.lab56.client.repl.input.UserInputProvider;
import ru.ifmo.se.s267880.lab56.shared.HandlerCallback;
import ru.ifmo.se.s267880.lab56.shared.Config;
import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandController;
import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandHandler;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.CommandHandlers;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.ReflectionCommandHandlerGenerator;
import ru.ifmo.se.s267880.lab56.shared.communication.*;
import ru.ifmo.se.s267880.lab56.shared.sharedCommandHandlers.CollectionManipulationCommandHandlers;
import ru.ifmo.se.s267880.lab56.shared.sharedCommandHandlers.MiscellaneousCommandHandlers;
import ru.ifmo.se.s267880.lab56.shared.sharedCommandHandlers.StoringAndRestoringCommandHandlers;
import ru.ifmo.se.s267880.lab56.shared.sharedCommandHandlers.UserAccountManipulationCommandHandlers;
import sun.misc.Unsafe;

import static ru.ifmo.se.s267880.lab56.client.UserInputHelper.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Tran Quang Loc
 */
public class Main {
    public static InetSocketAddress address = null;
    public static void main(String[] args) {
        removeGSONNonExecutablePrefix();
        printAwesomeASCIITitle();

        if (address == null) {
            address = getInitSocketAddressFromUserInput("localhost", Config.COMMAND_EXECUTION_PORT);
        }

        SocketConnector socketConnector = new SocketConnector(5, 1300);
        HandlerCallback<SocketChannel> socketConnectorCallback = new HandlerCallback<SocketChannel>() {
            @Override
            public void onSuccess(SocketChannel sc) {
                new MainLoop(createCommandController(sc), createUserInputProvider()).run(
                    HandlerCallback.ofErrorHandler(e -> socketConnector.tryConnectTo(address, this))
                );
            }

            private UserInputProvider createUserInputProvider() {
                return new UserInputProvider(new InputStreamReader(System.in),
                        new JsonUserInputArgumentParser(),
                        new DefaultUserInputArgumentParser()
                );
            }

            @Override
            public void onError(Exception e) {
                if (e instanceof InterruptedException) {
                    // because we are trying to connect to server so the socket channel is not need to be closed.
                    return ;
                }
                ConsoleWrapper.console.printf(e instanceof  UnresolvedAddressException
                        ? "Address %s can not be resolved%n"
                        : "Unable to connect to %s%n", address);
                if (confirm("Reenter address?")) {
                    address = getInitSocketAddressFromUserInput(address.getHostName(), address.getPort());
                    socketConnector.tryConnectTo(address, this);
                }
            }
        };
        socketConnector.tryConnectTo(address, socketConnectorCallback);
    }

    private static CommandController createCommandController(SocketChannel sc) {
        CommandController cc = new CommandController();
        Broadcaster<MessageType> messageFromServerBroadcaster = new Broadcaster<>(Receiver.fromSocketChannel(sc));
        Sender messageToServerSender = Sender.fromSocketChannel(sc);
        new Thread(messageFromServerBroadcaster).start();

        Consumer[] listeners = new Consumer[3];
        listeners[0] = messageFromServerBroadcaster.onError.listen(e -> {
            messageFromServerBroadcaster.whenReceive(MessageType.NOTIFICATION).removeListener(listeners[1]);
            messageFromServerBroadcaster.whenReceive(MessageType.REQUEST).removeListener(listeners[2]);
            messageFromServerBroadcaster.onError.removeListener(listeners[0]);
        });
        listeners[1] = messageFromServerBroadcaster.whenReceive(MessageType.NOTIFICATION).listen(m -> {
            if (!(m instanceof UserNotification)) return;
            ConsoleWrapper.console.printf("\r>> %s%n> ", m);
        });
        listeners[2] = messageFromServerBroadcaster.whenReceive(MessageType.REQUEST).listen(res -> {
            if (!(res instanceof TokenRequest)) return;
            ConsoleWrapper.console.println(((TokenRequest) res).getDisplayingMessage());
            String token = ConsoleWrapper.console.readLine("Enter your token (enter `\\abort` to cancel this operation):");
            try {
                messageToServerSender.sendWithChannel(new Respond(MessageType.RESPOND_SUCCESS, "Token:" + token, null));
            } catch (IOException ignore) {
                // TODO make this part less tricky
            }
        });

        Supplier<CommandToServerExecutor> commandExecutorSupplier = () ->
                new CommandToServerExecutor(messageFromServerBroadcaster, messageToServerSender);
        Map<Class, CommandHandlers> handlersMap = new HashMap<>();
        handlersMap.put(CollectionManipulationCommandHandlers.class, new ClientCollectionManipulationCommandHandlers(commandExecutorSupplier));
        handlersMap.put(StoringAndRestoringCommandHandlers.class, new ClientStoringAndRestoringCommandHandlers(commandExecutorSupplier));
        handlersMap.put(UserAccountManipulationCommandHandlers.class, new ClientUserAccountManipulationCommandHandlers(commandExecutorSupplier));
        handlersMap.put(MiscellaneousCommandHandlers.class, new ClientMiscellaneousCommandHandlers(commandExecutorSupplier));

        handlersMap.forEach((cls, handlers) -> {
            ReflectionCommandHandlerGenerator.generate(cls, handlers, new ClientInputPreprocessor())
                    .forEach(cc::addCommand);

            // also add additional handlers that not defined in the shard interfaces.
            ReflectionCommandHandlerGenerator.generate(handlers.getClass(), handlers, new ClientInputPreprocessor())
                    .forEach(cc::addCommand);
        });

        cc.addCommand("exit", CommandHandler.ofConsumer( "I don't have to explain :) [Additional].", args -> {
            try {sc.close(); } catch (Exception ignored) {}
            System.exit(0);
        }));

        cc.addCommand("list-commands", CommandHandler.ofConsumer(
                "[Additional] List all the commands.", args -> {
                    ConsoleWrapper.console.println("# Commands list:");
                    cc.getCommandHandlers().forEach((commandName, handler) -> {
                        ConsoleWrapper.console.printf("- ");
                        for (String s : handler.getUsage(commandName).split("\n")) {
                            ConsoleWrapper.console.printf("\t%s\n", s);
                        }
                        ConsoleWrapper.console.println("");
                    });
                }
        ));
        return cc;
    }

    private static void printAwesomeASCIITitle() {
        ConsoleWrapper.console.println("  ___ ___    ___    ___ ______  ____  ____    ____      ___ ___   ____  ____    ____   ____    ___  ____");
        ConsoleWrapper.console.println("_|   |   |  /  _]  /  _]      ||    ||    \\  /    |    |   |   | /    ||    \\  /    | /    |  /  _]|    \\");
        ConsoleWrapper.console.println("_| _   _ | /  [_  /  [_|      | |  | |  _  ||   __|    | _   _ ||  o  ||  _  ||  o  ||   __| /  [_ |  D  )");
        ConsoleWrapper.console.println("_|  \\_/  ||    _]|    _]_|  |_| |  | |  |  ||  |  |    |  \\_/  ||     ||  |  ||     ||  |  ||    _]|    /");
        ConsoleWrapper.console.println("_|   |   ||   [_ |   [_  |  |   |  | |  |  ||  |_ |    |   |   ||  _  ||  |  ||  _  ||  |_ ||   [_ |    \\");
        ConsoleWrapper.console.println("_|   |   ||     ||     | |  |   |  | |  |  ||     |    |   |   ||  |  ||  |  ||  |  ||     ||     ||  .  \\");
        ConsoleWrapper.console.println("_|___|___||_____||_____| |__|  |____||__|__||___,_|    |___|___||__|__||__|__||__|__||___,_||_____||__|\\_|");
        ConsoleWrapper.console.println("Use \"help\" to display the help message. Use \"list-commands\" to display all the commands.");
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
     *     number or is an load brace. But the second problem is that I want the command to be execute with more than
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
    private static boolean removeGSONNonExecutablePrefix() {
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
        }

        try {
            Field nonExecutePrefixField = JsonReader.class.getDeclaredField("NON_EXECUTE_PREFIX");
            nonExecutePrefixField.setAccessible(true);

            Field modifier = Field.class.getDeclaredField("modifiers");
            modifier.setAccessible(true);

            modifier.set(nonExecutePrefixField,nonExecutePrefixField.getModifiers() & ~Modifier.FINAL);
            nonExecutePrefixField.set(null, new char[]{});
        } catch (IllegalAccessException | NoSuchFieldException e) {
            return false;
        }
        return true;
    }
}

