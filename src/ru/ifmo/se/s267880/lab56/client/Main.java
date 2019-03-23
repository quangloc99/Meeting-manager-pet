package ru.ifmo.se.s267880.lab56.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Main {
    public static void main(String[] args) throws Exception {
        Socket s = new Socket("127.0.0.1", 3499);
        System.err.println("found server");
        PrintWriter out = new PrintWriter(s.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        System.out.println("writing");
        out.println("Loc :)");
        System.out.println("done writing");
        System.out.println(in.readLine());
        in.close();
        out.close();
        s.close();
    }
}
