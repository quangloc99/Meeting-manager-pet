package ru.ifmo.se.s267880.lab5;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;
import com.google.gson.stream.JsonReader;
import ru.ifmo.se.s267880.lab5.commandControllerHelper.*;

import java.io.InputStreamReader;
import java.rmi.server.ExportException;

class DefinedCommands {
    @Command(usage = "just say hello")
    public void hi() {
        System.out.println("hello");
    }

    @Command(usage = "say hello to someone in the input")
    public void hello(String name) {
        System.out.println("hello " + name);
    }
}

public class Main {
    public static void main(String[] args) {
        CommandController cc = new CommandController();
        cc.addCommand("exit", "[Additional] I don't have to explain :).", () -> System.exit(0));
        MeetingManager mm = new MeetingManager("aFile.csv");
        ReflectionCommandAdder.addCommand(cc, mm, new MeetingManagerInputPreprocessor());
        while (true) {
            try {
                cc.prompt();
                mm.save();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
