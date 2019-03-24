package ru.ifmo.se.s267880.lab56;

/**
 * @author Tran Quang Loc
 */
public class Main {
    public static void main(String args[]) {
        new Thread(() -> {
            ru.ifmo.se.s267880.lab56.client.Main.main(args);
        }).start();

        new Thread(() -> {
            ru.ifmo.se.s267880.lab56.server.Main.main(args);
        }).start();
    }
}
