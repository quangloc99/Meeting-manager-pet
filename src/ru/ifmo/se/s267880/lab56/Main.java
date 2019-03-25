package ru.ifmo.se.s267880.lab56;

import ru.ifmo.se.s267880.lab56.shared.Config;

import java.net.InetSocketAddress;

/**
 * @author Tran Quang Loc
 */
public class Main {
    public static void main(String args[]) {
        new Thread(() -> {
            ru.ifmo.se.s267880.lab56.server.Main.main(args);
        }).start();

        new Thread(() -> {
            ru.ifmo.se.s267880.lab56.client.Main.address = new InetSocketAddress("localhost", Config.COMMAND_EXECUTION_PORT);
            ru.ifmo.se.s267880.lab56.client.Main.main(args);
        }).start();
    }
}
