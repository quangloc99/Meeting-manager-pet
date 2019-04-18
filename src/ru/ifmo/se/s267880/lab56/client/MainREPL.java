package ru.ifmo.se.s267880.lab56.client;

import ru.ifmo.se.s267880.lab56.shared.CommunicationIOException;
import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandController;

abstract public class MainREPL extends Thread {
    private CommandController cc;

    public MainREPL(CommandController cc) {
        this.cc = cc;
    }

    public void run() {
        cc.execute(this::onSuccessfulExecuted, this::onError);
    }

    private void onSuccessfulExecuted(Object o) {
        new Thread(this).start();
    }

    private void onError(Exception e) {
        System.err.printf("Error: %s\n", e.getMessage());
        Throwable cause = e.getCause();
        if (cause instanceof CommunicationIOException) {
            System.err.println("Disconnected to server");
            onDisconnectedToServer(cause);
            return ;
        }
        new Thread(this).start();
    }

    abstract public void onDisconnectedToServer(Throwable e);
}
