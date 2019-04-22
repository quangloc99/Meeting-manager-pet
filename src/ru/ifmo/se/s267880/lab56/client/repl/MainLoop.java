package ru.ifmo.se.s267880.lab56.client.repl;

import ru.ifmo.se.s267880.lab56.client.ConsoleWrapper;
import ru.ifmo.se.s267880.lab56.client.repl.input.UserInputProvider;
import ru.ifmo.se.s267880.lab56.shared.HandlerCallback;
import ru.ifmo.se.s267880.lab56.shared.communication.CommunicationIOException;
import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandController;

import java.util.function.Consumer;

public class MainLoop extends Thread {
    private CommandController cc;
    private UserInputProvider inputProvider;

    public MainLoop(CommandController cc, UserInputProvider inputProvider) {
        this.cc = cc;
        this.inputProvider = inputProvider;
    }

    public void run(HandlerCallback<Void> callback) {
        new Thread(() -> {
            Consumer<Exception> onError = e -> {
                ConsoleWrapper.console.printf("Error: %s\n", e.getMessage());
                Throwable cause = e.getCause();
                if (cause instanceof CommunicationIOException) {
                    ConsoleWrapper.console.println("Disconnected to server");
                    callback.onError((Exception) cause);
                    return;
                }
                this.run(callback);
            };
            ConsoleWrapper.console.printf("> ");
            this.inputProvider.getInput(new HandlerCallback<>(
                    args -> {
                        if (args.size() == 0) this.run(callback);   // there is no command at all.
                        else if (!(args.get(0) instanceof String)) onError.accept(new Exception("Command must be a string."));
                        else cc.execute((String) args.get(0), args.subList(1, args.size()),
                                new HandlerCallback<>(o -> this.run(callback), onError));
                    }, onError
            ));
        }).start();
    }
}
