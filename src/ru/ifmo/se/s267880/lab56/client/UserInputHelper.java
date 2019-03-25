package ru.ifmo.se.s267880.lab56.client;

import java.net.InetSocketAddress;
import java.util.Scanner;

public class UserInputHelper {
    static boolean confirm(String prompt) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print(prompt + " (Y/n)");
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
            System.out.printf("Enter host name (default %s): ", defaultHostName);
            hostName = sc.nextLine();
            if (hostName.isEmpty()) hostName = defaultHostName;
            System.out.printf("Enter port number (default %d): ", defaultPort);
            try {
                port = Integer.parseInt(sc.nextLine());
                if (port < 0 || port > 65535) {
                    port = defaultPort;
                }
            } catch (NumberFormatException e) {
                port = defaultPort;
            }
        } while (!confirm(String.format("Connect to %s with port %d ('n' to reenter address)?", hostName, port)));
        return new InetSocketAddress(hostName, port);
    }
}
