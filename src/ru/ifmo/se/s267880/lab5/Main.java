package ru.ifmo.se.s267880.lab5;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import ru.ifmo.se.s267880.lab5.csv.CsvParser;
import ru.ifmo.se.s267880.lab5.csv.CsvRowWriter;
import ru.ifmo.se.s267880.lab5.csv.CsvRowWithNamesWriter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.rmi.server.ExportException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) {
//        String csv = "name,\"high score\",time\r\n" +
//                "\"Tran Quang Loc\",1000,Dec 2019\r\n" +
//                "\"Nguyen\"\" Vinh\"\"Thinh\", 2000 , Jan 2020\r\n" +
//                ",,\r\n" +
//                "cool,,yeah";
//        String json = "{\n" +
//                "\"name\": \"abc xyz\"\n," +
//                "\"age\": 100,\n" +
//                "\"films\": [\n" +
//                "   \"adventure time\",\n" +
//                "   \"svtfoe\"\n" +
//                "]\n" +
//                "}\n [1, 2, 3, 4]";
//        InputStreamReader msc = new InputStreamReader(System.in);
//        StringReader sr = new StringReader(json) ;
//        JsonReader jreader = new JsonReader(sr);
////        jreader.setLenient(false);
//        JsonElement je = (new JsonParser()).parse(jreader);
//        System.out.println(json.length());
//        System.out.printf("%20s", je.getAsJsonObject().get("name").getAsString());
        CommandController cc = new CommandController();
        cc.addCommand("exit", "escape the program", () -> System.exit(0));
        while (true) {
            try {
                cc.prompt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
