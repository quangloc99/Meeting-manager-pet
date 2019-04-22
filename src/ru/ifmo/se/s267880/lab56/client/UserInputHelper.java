package ru.ifmo.se.s267880.lab56.client;

import java.io.Console;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

public class UserInputHelper {
    public static final String clearLine = "\r" + String.join("", Collections.nCopies(100, " ")) + "\r";
    static boolean confirm(String prompt) {
        while (true) {
            ConsoleWrapper.console.printf(clearLine + prompt + " (Y/n)");
            String line = ConsoleWrapper.console.readLine();
            if (line.isEmpty()) return true;
            if (line.length() > 2) continue;
            char t = Character.toUpperCase(line.charAt(0));
            if (t != 'Y' && t != 'N') continue;
            return t == 'Y';
        }
    }

    static InetSocketAddress getInitSocketAddressFromUserInput(String defaultHostName, int defaultPort) {
        String hostName;
        int port;
        do {
            ConsoleWrapper.console.printf(clearLine + "Enter host name (default %s): ", defaultHostName);
            hostName = ConsoleWrapper.console.readLine();
            if (hostName.isEmpty()) hostName = defaultHostName;
            System.out.printf(clearLine + "Enter port number (default %d): ", defaultPort);
            try {
                port = Integer.parseInt(ConsoleWrapper.console.readLine());
                if (port < 0 || port > 65535) {
                    port = defaultPort;
                }
            } catch (NumberFormatException e) {
                port = defaultPort;
            }
        } while (!confirm(String.format(clearLine + "Connect to %s with port %d ('n' to reenter address)?", hostName, port)));
        return new InetSocketAddress(hostName, port);
    }

    static char[] getCheckedPassword() {
        char[] pass1 = null;
        char[] pass2 = null;
        while (true) {
            pass1 = ConsoleWrapper.console.readPassword("Enter the password (abort with blank password): ");
            if (pass1.length == 0) {
                return null;
            }
            pass2 = ConsoleWrapper.console.readPassword("Enter it again: ");
            if (Arrays.equals(pass1, pass2)) break;
            ConsoleWrapper.console.println("Password is not the same!");
            if (pass1 != null) Arrays.fill(pass1, '\0');
            if (pass2 != null) Arrays.fill(pass2, '\0');
        }
        Arrays.fill(pass2, '\0');
        return pass1;
    }
}
