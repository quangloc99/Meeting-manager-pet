package ru.ifmo.se.s267880.lab56.server;

import ru.ifmo.se.s267880.lab56.shared.QueryToServer;

import javax.management.Query;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) throws Exception {
        ServerSocket ss = new ServerSocket(3499);
        System.err.println("waiting for client");
        Socket client = ss.accept();
        System.err.println("found client!");
        PrintWriter out = new PrintWriter(client.getOutputStream(), true);
        ObjectInputStream in = new ObjectInputStream(client.getInputStream());
        QueryToServer qr = (QueryToServer) in.readObject();
        System.out.println(qr.getName());

        in.close();
        out.close();
        client.close();
        ss.close();
    }
}
