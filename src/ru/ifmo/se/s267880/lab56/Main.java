package ru.ifmo.se.s267880.lab56;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
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
            try (FileInputStream configFile = new FileInputStream("config.json")) {
                JsonElement elm = new JsonParser().parse(new InputStreamReader(configFile));
                int NPORT = elm.getAsJsonObject().get("port").getAsInt();
                ru.ifmo.se.s267880.lab56.client.Main.address = new InetSocketAddress("localhost", NPORT);
                ru.ifmo.se.s267880.lab56.client.Main.main(args);
            } catch (FileNotFoundException e) {
                System.err.println("config.json not found");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
