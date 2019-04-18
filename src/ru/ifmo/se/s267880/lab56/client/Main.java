package ru.ifmo.se.s267880.lab56.client;

import ru.ifmo.se.s267880.lab56.client.repl.MainLoop;
import ru.ifmo.se.s267880.lab56.client.repl.input.DefaultUserInputArgumentParser;
import ru.ifmo.se.s267880.lab56.client.repl.input.JsonUserInputArgumentParser;
import ru.ifmo.se.s267880.lab56.client.repl.input.UserInputProvider;
import ru.ifmo.se.s267880.lab56.shared.HandlerCallback;
import ru.ifmo.se.s267880.lab56.shared.SharedCommandHandlers;
import ru.ifmo.se.s267880.lab56.shared.Config;
import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandController;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.ReflectionCommandAdder;
import static ru.ifmo.se.s267880.lab56.client.UserInputHelper.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.util.Arrays;

/**
 * @author Tran Quang Loc
 */
public class Main {
    public static SocketChannel sc = null;
    public static InetSocketAddress address = null;
    private static HandlerCallback<SocketChannel> socketConnectorCallback;
    public static void main(String[] args) {
        printAwesomeASCIITitle();

        CommandController cc = new CommandController();
        UserInputProvider inputProvider = new UserInputProvider(new InputStreamReader(System.in),
                new JsonUserInputArgumentParser(),
                new DefaultUserInputArgumentParser()
        );
        ClientCommandsHandlers handlers = new ClientCommandsHandlers() {
            public SocketChannel getChannel() throws IOException  {
                return sc;
            }
        };
        addClientOnlyCommand(cc, handlers);
        ReflectionCommandAdder.addCommand(cc, SharedCommandHandlers.class, handlers, new ClientInputPreprocessor());

        if (address == null) {
            address = getInitSocketAddressFromUserInput("localhost", Config.COMMAND_EXECUTION_PORT);
        }

        SocketConnector socketConnector = new SocketConnector(5, 1300);
        socketConnectorCallback = new HandlerCallback<>(
            sc -> {
                Main.sc = sc;
                new MainLoop(cc, inputProvider).run(HandlerCallback.ofErrorHandler(
                        e -> socketConnector.tryConnectTo(address, socketConnectorCallback)
                ));
            },
            e -> {
                if (e instanceof InterruptedException) {
                    onExit();
                    return ;
                }
                System.err.printf(e instanceof  UnresolvedAddressException
                        ? "Address %s can not be resolved%n"
                        : "Unable to connect to %s%n", address);
                if (confirm("Reenter address?")) {
                    address = getInitSocketAddressFromUserInput(address.getHostName(), address.getPort());
                    socketConnector.tryConnectTo(address, socketConnectorCallback);
                }
            }
        );
        socketConnector.tryConnectTo(address, socketConnectorCallback);
    }

    static void printAwesomeASCIITitle() {
        System.out.println("  ___ ___    ___    ___ ______  ____  ____    ____      ___ ___   ____  ____    ____   ____    ___  ____");
        System.out.println("_|   |   |  /  _]  /  _]      ||    ||    \\  /    |    |   |   | /    ||    \\  /    | /    |  /  _]|    \\");
        System.out.println("_| _   _ | /  [_  /  [_|      | |  | |  _  ||   __|    | _   _ ||  o  ||  _  ||  o  ||   __| /  [_ |  D  )");
        System.out.println("_|  \\_/  ||    _]|    _]_|  |_| |  | |  |  ||  |  |    |  \\_/  ||     ||  |  ||     ||  |  ||    _]|    /");
        System.out.println("_|   |   ||   [_ |   [_  |  |   |  | |  |  ||  |_ |    |   |   ||  _  ||  |  ||  _  ||  |_ ||   [_ |    \\");
        System.out.println("_|   |   ||     ||     | |  |   |  | |  |  ||     |    |   |   ||  |  ||  |  ||  |  ||     ||     ||  .  \\");
        System.out.println("_|___|___||_____||_____| |__|  |____||__|__||___,_|    |___|___||__|__||__|__||__|__||___,_||_____||__|\\_|");
        System.out.println("Use \"help\" to display the help message. Use \"list-commands\" to display all the commands.");
    }

    static void addClientOnlyCommand(CommandController cc, ClientCommandsHandlers handers) {
        cc.addCommand("toggle-quite", "Turn on/off printing collections after successfully executed a command [Additional]", args -> {
            handers.toggleQuite();
            return null;
        });
        cc.addCommand("exit", "I don't have to explain :) [Additional].", arg -> {
            onExit();
            System.exit(0);
            return null;
        });
        cc.addCommand("clrscr", "Clear the screen. [Additional]", arg-> {
            try {
                System.err.println("clrscr command will not work on IntelliJ IDE");
                final String os = System.getProperty("os.name");
                if (os.contains("Windows")) {
                    Runtime.getRuntime().exec("cls");
                } else {
                    System.out.print("\033[H\033[2J");
                    System.out.flush();
                    Runtime.getRuntime().exec("clear");
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
            return null;
        });
        cc.addCommand("help", "Display the help message, the arg json format. [Additional]", args -> {
            help();
            return null;
        });

        cc.addCommand("list-commands", "[Additional] List all the commands.", (Object[] args) -> {
            System.out.println("# Commands list:");
            cc.getCommandHandlers().forEach((commandName, handler) -> {
                System.out.printf("- %s\n", commandName);
                for (String s : handler.getUsage().split("\n")) {
                    System.out.printf("\t%s\n", s);
                }
                System.out.println();
            });
            return null;
        });
    }

    public static void onExit() {
        try { sc.close(); } catch (IOException e) {}

    }

    /**
     * Print a help message.
     */
    public static void help() {
        String[] helps = {
                "# Help",
                "\tUse command \"help\" to display this message.",
                "\tUse command \"list-commands\" for the full list of commands.",
                "# Argument formats",
                "## MeetingJson",
                "\t{",
                "\t\t\"name\"    : String,                This field is required",
                "\t\t\"time\"    : DateJson,              Default value is the current time",
                "\t\t\"duration\": minutes_in_number,     Default value is 60",
                "\t\t\"location\": LocationJson,          Default value is the 1-st floor of the 1-st building [1, 1]",
                "\t}",
                "",
                "## DateJson",
                "\tDateJson can have 1 of 3 following forms:",
                "\t1) [int, int, int, int, int, int] - from left to right: year, month, date, hour, minute, second",
                "\t2) String representation with the following format: \"yyyy/MM/dd HH:mm:ss\"",
                "\t3) Object representation:",
                "\t{",
                "\t\t\"year\": int,",
                "\t\t\"month\": int,",
                "\t\t\"date\": int,",
                "\t\t\"hour\": int,",
                "\t\t\"minute\": int,",
                "\t\t\"second\": int",
                "\t}",
                "\tIn the 1-st and 3-rd form, if a field is missing, it will be filled with zero or by the current time's values",
                "",
                "## LocaltionJson",
                "\tRight now this app supports very simple location. A location consists of only 2 value:",
                "\tthe building number and the floor number that the meeting will be held.",
                "\tLocationJson can have 1 of 2 forms:",
                "\t1) [int, int] - from left to right is the building number and then the floor number.",
                "\t2) Object representation:",
                "\t{",
                "\t\t\"building\": int,                   Default value is 1",
                "\t\t\"floor\"   : int,                   Default value is 1",
                "\t}",
        };
        Arrays.stream(helps).forEach(System.out::println);
    }

}

