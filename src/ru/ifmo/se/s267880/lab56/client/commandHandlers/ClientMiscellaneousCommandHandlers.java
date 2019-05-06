package ru.ifmo.se.s267880.lab56.client.commandHandlers;

import ru.ifmo.se.s267880.lab56.client.ConsoleWrapper;
import ru.ifmo.se.s267880.lab56.client.Main;
import ru.ifmo.se.s267880.lab56.client.Services;
import ru.ifmo.se.s267880.lab56.shared.HandlerCallback;
import ru.ifmo.se.s267880.lab56.shared.Helper;
import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandHandler;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.Command;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.InputPreprocessor;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.ReflectionCommandHandlerGenerator;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.Usage;
import ru.ifmo.se.s267880.lab56.shared.communication.CommandExecuteRequest;
import ru.ifmo.se.s267880.lab56.shared.sharedCommandHandlers.MiscellaneousCommandHandlers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.util.*;

public class ClientMiscellaneousCommandHandlers
    extends ClientCommandsHandlers
    implements MiscellaneousCommandHandlers
{
    public ClientMiscellaneousCommandHandlers(Services services) {
        super(services);
    }

    /**
     * show file name, number of meeting and the time the file first load during this session.
     */
    @Override
    public void info(HandlerCallback<Map<String, String>> callback) {
        buildCommandExecutor().run(generateRequest(), new HandlerCallback<>(res -> {
            Map<String, String> result = res.getResult();
            ConsoleWrapper.console.println("# Information");
            ConsoleWrapper.console.println("User email: " + (result.get("user-email") == null ? "<<you are currently logging out>>" : result.get("user-email")));
            ConsoleWrapper.console.println("Collection name: " + (result.get("file") == null ? "<<no name>>" : result.get("file")));
            ConsoleWrapper.console.println("Sort order: " + result.get("sort-order"));
            ConsoleWrapper.console.println("Time zone: " + result.get("time-zone"));
            ConsoleWrapper.console.println("Number of meeting: " + result.get("meeting-count"));
            ConsoleWrapper.console.println("Load since: " + result.get("since"));
//            ConsoleWrapper.console.println("Is quite: " + isQuite);
            callback.onSuccess(result);
        }, callback::onError));
    }

    @Override
    public void listTimeZones(HandlerCallback<List<TimeZone>> callback) {
        buildCommandExecutor().run(generateRequest(), new HandlerCallback<>(res -> {
            sortAndPrintTimezones(res.getResult());
            callback.onSuccess(res.getResult());
        }, callback::onError));
    }

    @Override
    public void listTimeZones(int offset, HandlerCallback<List<TimeZone>> callback) {
        buildCommandExecutor().run(generateRequest(), new HandlerCallback<>(res -> {
            sortAndPrintTimezones(res.getResult());
            callback.onSuccess(res.getResult());
        }, callback::onError));
    }

    private void sortAndPrintTimezones(List<TimeZone> zones) {
        zones.sort(Comparator.comparing(TimeZone::getRawOffset).thenComparing(TimeZone::getID));
        zones.forEach(s -> ConsoleWrapper.console.printf("\t%s: %s\n",
                Helper.timeZoneToGMTString(s), s.getID()
        ));
    }

    @Override
    public void setTimeZone(String zoneId, HandlerCallback callback) {
        buildCommandExecutor().run(generateRequest(), callback);
    }

    @Command("set-timezone")
    @Usage("automatically detect and set the time zone to your current time zone.")
    public void setTimeZone(HandlerCallback callback) {
        buildCommandExecutor().run(new CommandExecuteRequest(getCommandName(), ZonedDateTime.now().getZone().toString()), callback);
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
            ConsoleWrapper.console.println("No help found. Contact the developer if you see this message.");
        } else {
            new BufferedReader(new InputStreamReader(help)).lines().forEach(ConsoleWrapper.console::println);
        }
    }

    @Override
    public Map<String, CommandHandler> generateHandlers(InputPreprocessor preprocessor) {
        Map<String, CommandHandler> res = MiscellaneousCommandHandlers.super.generateHandlers(preprocessor);
        ReflectionCommandHandlerGenerator.generate(
            ClientMiscellaneousCommandHandlers.class, this, preprocessor
        ).forEach((commandName, handler) -> res.merge(commandName, handler, CommandHandler::join));
        return res;
    }
}
