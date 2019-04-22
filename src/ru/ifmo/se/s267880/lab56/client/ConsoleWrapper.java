package ru.ifmo.se.s267880.lab56.client;

import java.io.Console;
import java.util.Scanner;

public class ConsoleWrapper {
    public static ConsoleWrapper console = new ConsoleWrapper();

    private Object ioLock = new Object();
    private Console trueConsole = System.console();
    private Scanner scanner;
    private ConsoleWrapper() {
        if (trueConsole == null) {
            scanner = new Scanner(System.in);
            System.err.println("Warning! This program is running on a non-console environment, so some function might be limited.");
        }
    }

    public String readLine() { return readLine(""); }

    public String readLine(String fmt, Object... objs) {
        if (trueConsole != null) return trueConsole.readLine(fmt, objs);
        else {
            synchronized (ioLock) {
                System.out.printf(fmt, objs);
                return scanner.nextLine();
            }
        }
    }

    public char[] readPassword() { return readPassword(""); }
    public char[] readPassword(String fmt, Object... objs) {
        if (trueConsole != null) return trueConsole.readPassword(fmt, objs);
        else {
            synchronized (ioLock) {
                System.out.printf(fmt, objs);
                String str = scanner.nextLine();
                char[] res = new char[str.length()];
                str.getChars(0, str.length(), res, res.length);
                return res;
            }
        }
    }

    public void println(String s) { printf(s + "\n"); }
    public void printf(String fmt, Object... objs) {
        if (trueConsole != null) trueConsole.printf(fmt, objs);
        else {
            synchronized (ioLock) {
                System.out.printf(fmt, objs);
            }
        }
    }

    public void flush() {
        if (trueConsole != null) trueConsole.flush();
        else {
            synchronized (ioLock) {
                System.out.flush();
            }
        }
    }
}
