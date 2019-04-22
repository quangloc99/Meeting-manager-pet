package ru.ifmo.se.s267880.lab56.client;

import java.io.Console;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Scanner;

public class UserInputHelper {
    public static final String clearLine = "\r" + String.join("", Collections.nCopies(100, " ")) + "\r";
    static boolean confirm(String prompt) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print(clearLine + prompt + " (Y/n)");
            String line = sc.nextLine();
            if (line.isEmpty()) return true;
            if (line.length() > 2) continue;
            char t = Character.toUpperCase(line.charAt(0));
            if (t != 'Y' && t != 'N') continue;
            return t == 'Y';
        }
    }

    static InetSocketAddress getInitSocketAddressFromUserInput(String defaultHostName, int defaultPort) {
        Scanner sc = new Scanner(System.in);
        String hostName;
        int port;
        do {
            System.out.printf(clearLine + "Enter host name (default %s): ", defaultHostName);
            hostName = sc.nextLine();
            if (hostName.isEmpty()) hostName = defaultHostName;
            System.out.printf(clearLine + "Enter port number (default %d): ", defaultPort);
            try {
                port = Integer.parseInt(sc.nextLine());
                if (port < 0 || port > 65535) {
                    port = defaultPort;
                }
            } catch (NumberFormatException e) {
                port = defaultPort;
            }
        } while (!confirm(String.format(clearLine + "Connect to %s with port %d ('n' to reenter address)?", hostName, port)));
        return new InetSocketAddress(hostName, port);
    }

    static char[] getPassword(String prompt) {
        Console con = System.console();
        return con.readPassword(clearLine + prompt);
    }
}
