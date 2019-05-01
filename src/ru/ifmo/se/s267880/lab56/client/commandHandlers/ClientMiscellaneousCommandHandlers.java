package ru.ifmo.se.s267880.lab56.client.commandHandlers;

import ru.ifmo.se.s267880.lab56.client.ConsoleWrapper;
import ru.ifmo.se.s267880.lab56.client.Main;
import ru.ifmo.se.s267880.lab56.shared.HandlerCallback;
import ru.ifmo.se.s267880.lab56.shared.ZoneUtils;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.Command;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.Usage;
import ru.ifmo.se.s267880.lab56.shared.sharedCommandHandlers.MiscellaneousCommandHandlers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.ZoneId;
import java.util.Map;
import java.util.function.Supplier;

public class ClientMiscellaneousCommandHandlers
    extends ClientCommandsHandlers
    implements MiscellaneousCommandHandlers
{
    public ClientMiscellaneousCommandHandlers(Supplier<CommandToServerExecutor> commandExecutorSupplier) {
        super(commandExecutorSupplier);
    }
    /**
     * show file name, number of meeting and the time the file first load during this session.
     */
    @Override
    public void info(HandlerCallback<Map<String, String>> callback) {
        buildCommandExecutor().run(generateRequest(), new HandlerCallback<>(res -> {
            Map<String, String> result = res.getResult();
            ConsoleWrapper.console.println("# Information");
            ConsoleWrapper.console.println("User email: " + result.get("user-email"));
            ConsoleWrapper.console.println("File name: " + (result.get("file") == null ? "<<no name>>" : result.get("file")));
            ConsoleWrapper.console.println("Time zone: UTC" + result.get("time-zone"));
            ConsoleWrapper.console.println("Number of meeting: " + result.get("meeting-count"));
            ConsoleWrapper.console.println("File load since: " + result.get("since"));
//            ConsoleWrapper.console.println("Is quite: " + isQuite);
            callback.onSuccess(result);
        }, callback::onError));
    }

    @Override
    public void listTimeZones(int offset, HandlerCallback<Map<Integer, ZoneId>> callback) {
        buildCommandExecutor().run(generateRequest(), new HandlerCallback<>(res -> {
            ZoneUtils.printZonesByZoneOffset(res.getResult());
            callback.onSuccess(null);
        }, callback::onError));
    }

    @Override
    public void setTimeZone(int timeZoneKey, HandlerCallback callback) {
        buildCommandExecutor().run(generateRequest(), callback);
    }

    @Command
    @Usage("Clear the screen.")
    public void clrscr() {
        try {
            System.err.println("clrscr command will not work on IntelliJ IDE");
            final String os = System.getProperty("os.name");
            if (os.contains("Windows")) {
                Runtime.getRuntime().exec("cls");
            } else {
                ConsoleWrapper.console.printf("\033[H\033[2J");
                ConsoleWrapper.console.flush();
                Runtime.getRuntime().exec("clear");
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Print a help message.
     */
    @Command
    @Usage("Print help message, including the parameters json formats.")
    public static void help() {
        InputStream help = Main.class.getResourceAsStream("res/help.md");
        if (help == null) {
            ConsoleWrapper.console.println("No help found");
        } else {
            new BufferedReader(new InputStreamReader(help)).lines().forEach(ConsoleWrapper.console::println);
        }
    }
}
