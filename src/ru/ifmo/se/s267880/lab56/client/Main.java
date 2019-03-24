package ru.ifmo.se.s267880.lab56.client;

import ru.ifmo.se.s267880.lab56.shared.CommandHandlersWithMeeting;
import ru.ifmo.se.s267880.lab56.shared.Config;
import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandController;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.ReflectionCommandAdder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

/**
 * @author Tran Quang Loc
 */
public class Main {
    public static void main(String[] args) {
        printAwesomeASCIITitle();

        ClientCommandController cc = new ClientCommandController(System.in);
        ClientCommandsHandlers handlers = new ClientCommandsHandlers() {
            public SocketChannel createChannel() throws IOException  {
                SocketChannel sc = SocketChannel.open(new InetSocketAddress("127.0.0.1", Config.COMMAND_EXECUTION_PORT));
                return sc;
            }
        };
        addClientOnlyCommand(cc, handlers);
        if (!cc.removeGSONNonExecutablePrefix()) {
            System.err.println("cannot remove gson non execute prefix :(. " +
                    "If the command is hanging, please try press Enter multiple times.");
        }

        ReflectionCommandAdder.addCommand(cc, CommandHandlersWithMeeting.class, handlers, new ClientInputPreprocessor());

        while (true) {
            try {
                cc.execute();
            } catch (Exception e) {
                System.err.printf("Error: %s\n", e.getMessage());
            }
        }
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
        cc.addCommand("toggle-quite", "[Additional] Turn on/off printing collections after successfully executed a command", args -> {
            handers.toggleQuite();
            return null;
        });
        cc.addCommand("exit", "[Additional] I don't have to explain :).", arg -> {
            System.exit(0);
            return null;
        });
        cc.addCommand("clrscr", "[Additional] Clear the screen. ", arg-> {
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
        cc.addCommand("help", "[Additional] Display the help message, the arg json format.", Main::help);
    }

    /**
     * Print a help message.
     */
    public static Object help(Object[] args) {
        System.out.println("# Help");
        System.out.println("\tUse command \"help\" to display this message.");
        System.out.println("\tUse command \"list-commands\" for the full list of commands.");
        System.out.println();
        System.out.println("# Argument formats");
        System.out.println("## MeetingJson");
        System.out.println(
                "\t\t{\n" +
                        "\t\t\t\"name\"    : String,                       This field is required\n" +
                        "\t\t\t\"time\"    : DateJson,                     Default value is the current time\n" +
                        "\t\t\t\"duration\": minutes_in_number,            Default value is 60\n" +
                        "\t\t\t\"location\": LocationJson,                 Default value is the 1-st floor of the 1-st building [1, 1]\n" +
                "\t\t}"
        );
        System.out.println();
        System.out.println("## DateJson");
        System.out.println("\tDateJson can have 1 of 3 following forms:");
        System.out.println("\t1) [int, int, int, int, int, int] - from left to right: year, month, date, hour, minute, second");
        System.out.println("\t2) String representation with the following format: \"yyyy/MM/dd HH:mm:ss\"");
        System.out.println("\t3) Object representation:");
        System.out.println(
                "\t\t{\n" +
                        "\t\t\t\"year\": int,\n" +
                        "\t\t\t\"month\": int,\n" +
                        "\t\t\t\"date\": int,\n" +
                        "\t\t\t\"hour\": int,\n" +
                        "\t\t\t\"minute\": int,\n" +
                        "\t\t\t\"second\": int\n" +
                        "\t\t}"
        );
        System.out.println("\tIn the 1-st and 3-rd form, if a field is missing, it will be filled with zero or by the current time's values");
        System.out.println();
        System.out.println("## LocaltionJson");
        System.out.println("\tRight now this app supports very simple location. A location consists of only 2 value:");
        System.out.println("\tthe building number and the floor number that the meeting will be held.");
        System.out.println();
        System.out.println("\tLocationJson can have 1 of 2 forms:");
        System.out.println("\t1) [int, int] - from left to right is the building number and then the floor number.");
        System.out.println("\t2) Object representation:");
        System.out.println("\t\t{");
        System.out.println("\t\t\t\"building\": int,          Default value is 1");
        System.out.println("\t\t\t\"floor\"   : int,          Default value is 1");
        System.out.println("\t\t}\n");
        return null;
    }

}

