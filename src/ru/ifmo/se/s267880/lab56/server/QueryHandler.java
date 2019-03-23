package ru.ifmo.se.s267880.lab56.server;

import ru.ifmo.se.s267880.lab56.shared.QueryToServer;

import java.io.ObjectInputStream;
import java.net.Socket;

public class QueryHandler extends Thread {
    private Socket client;
    public QueryHandler(Socket socket) {
        System.err.println("found client!");
        this.client = socket;
    }

    @Override
    public void run() {
        try {
            //        PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            ObjectInputStream in = new ObjectInputStream(client.getInputStream());
            QueryToServer qr = (QueryToServer) in.readObject();
            System.out.println(qr.getName());

            in.close();
            //        out.close();
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
