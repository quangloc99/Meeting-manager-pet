package ru.ifmo.se.s267880.lab5;

import ru.ifmo.se.s267880.lab5.csv.CsvParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        String csv = "name,\"high score\",time\r\n" +
                "\"Tran,Quang,Loc\",1000,Dec 2019\r\n" +
                "\"Nguyen\"\" Vinh\"\"Thinh\", 2000 , Jan 2020\r\n" +
                ",,\r\n" +
                "cool,,yeah";
        try {
            CsvParser ps = new CsvParser(new ByteArrayInputStream(csv.getBytes()), true);
            Map<String, String> row;
            do {
                row = ps.getNextRowWithNames();
                System.out.println(row);
            } while (row != null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
