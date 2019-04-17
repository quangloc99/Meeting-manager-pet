package ru.ifmo.se.s267880.lab56.client;

import ru.ifmo.se.s267880.lab56.shared.CommunicationIOException;
import ru.ifmo.se.s267880.lab56.shared.EventEmitter;
import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandController;

public class MainREPL {
    public final EventEmitter<Throwable> disconnectedToServerEvent = new EventEmitter<>();
    private CommandController cc;

    public MainREPL(CommandController cc) {
        this.cc = cc;
        cc.commandSuccefulExecutedEvent.listen(o -> this.run());
        cc.errorEvent.listen(e -> {
            System.err.printf("Error: %s\n", e.getMessage());
            Throwable cause = e.getCause();
            if (cause instanceof CommunicationIOException) {
                System.err.println("Disconnected to server");
                disconnectedToServerEvent.emit(cause);
                return ;
            }
            this.run();
        });
    }

    public void run() {
        new Thread(cc::execute).start();
    }
}
