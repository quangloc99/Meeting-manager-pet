package ru.ifmo.se.s267880.lab56;

import ru.ifmo.se.s267880.lab56.client.ClientCommandsHandlers;
import ru.ifmo.se.s267880.lab56.server.ServerCommandsHandlers;
import ru.ifmo.se.s267880.lab56.shared.CommandHandlersWithMeeting;
import ru.ifmo.se.s267880.lab56.shared.Config;
import ru.ifmo.se.s267880.lab56.shared.Meeting;
import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandController;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.ReflectionCommandAdder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.LinkedList;

/**
 * @author Tran Quang Loc
 */
public class Main {
    /**
     * Print a help message.
     */
    public static Object help(Object[] args) {
        // TODO: add more helps
        System.out.println("# Help");
        System.out.println("\tUse command \"help\" to display this message.");
        System.out.println("\tUse command \"list-commands\" for the full list of commands.");
        System.out.println();
        System.out.println("## Arg json format");
        System.out.println("\tMostly the arg will be a json object that store the information of the meeting with the following format:");
        System.out.println(
                "\t\t{\n" +
                "\t\t\t\"name\": \"meeting name\",\n" +
                "\t\t\t\"time\": Date\n" +
                "\t\t}"
        );
        System.out.println("\tThe Date object can have 1 of 3 following formats:");
        System.out.println("\t1) [int, int, int, int, int, int] - This format is an array. Its elements from left to right represent");
        System.out.println("\tthe year, the month, the date, the hour, the minute and the seconds respectively.");
        System.out.println("\tThe array can have any size. If the size is less than 6, the other fields will be filled with");
        System.out.println("\tzero or current time's value.");
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
        System.out.println("\tlike array representation, if a field missing, it will be filled with zero or by the current time's values.");
        return null;
    }

    public static void main(String[] args) {
        System.out.println("  ___ ___    ___    ___ ______  ____  ____    ____      ___ ___   ____  ____    ____   ____    ___  ____");
        System.out.println("_|   |   |  /  _]  /  _]      ||    ||    \\  /    |    |   |   | /    ||    \\  /    | /    |  /  _]|    \\");
        System.out.println("_| _   _ | /  [_  /  [_|      | |  | |  _  ||   __|    | _   _ ||  o  ||  _  ||  o  ||   __| /  [_ |  D  )");
        System.out.println("_|  \\_/  ||    _]|    _]_|  |_| |  | |  |  ||  |  |    |  \\_/  ||     ||  |  ||     ||  |  ||    _]|    /");
        System.out.println("_|   |   ||   [_ |   [_  |  |   |  | |  |  ||  |_ |    |   |   ||  _  ||  |  ||  _  ||  |_ ||   [_ |    \\");
        System.out.println("_|   |   ||     ||     | |  |   |  | |  |  ||     |    |   |   ||  |  ||  |  ||  |  ||     ||     ||  .  \\");
        System.out.println("_|___|___||_____||_____| |__|  |____||__|__||___,_|    |___|___||__|__||__|__||__|__||___,_||_____||__|\\_|");

        String savedFileName = "untitled.csv";
        if (args.length > 0) {
            savedFileName = args[0];
        } else {
            System.out.println("No file name passed. Data will be read and saved into " + savedFileName);
        }
        System.out.println("Use \"help\" to display the help message. Use \"list-commands\" to display all the commands.");

        ServerCommandsHandlers mm = null;
        try {
            mm = new ServerCommandsHandlers(Collections.synchronizedList(new LinkedList<Meeting>()));
            mm.open(savedFileName);
            mm.save();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(0);
        }

        CLIWithJSONCommandController cc = new CLIWithJSONCommandController(System.in);
        if (!cc.removeGSONNonExecutablePrefix()) {
            System.err.println("cannot remove gson non execute prefix :(");
        }

        ReflectionCommandAdder.addCommand(
                cc,
                CommandHandlersWithMeeting.class,
                new ClientCommandsHandlers() {
                    public SocketChannel createChannel() throws IOException  {
                        SocketChannel sc = SocketChannel.open(new InetSocketAddress("127.0.0.1", Config.PORT));
                        return sc;
                    }
                },  // testing ClientCommandsHandlers
                new MeetingManagerInputPreprocessorJson()
        );
        cc.addCommand("exit", "[Additional] I don't have to explain :).", arg -> {
            System.exit(0);
            return null;
        });
        cc.addCommand("clrscr", "[Additional] Clear the screen. ", arg-> {
            try {
                final String os = System.getProperty("os.name");
                if (os.contains("Windows")) {
                    Runtime.getRuntime().exec("cls");
                }
                else {
                    System.err.println("this command will not work on IntelliJ IDE");
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
        while (true) {
            try {
                System.out.printf("%s> ", mm.getCurrentFileName());
                cc.execute();
                mm.save();
            } catch (Exception e) {
                System.err.printf("Error: %s\n", e.getMessage());
            }
        }
    }
}
