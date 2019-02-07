package ru.ifmo.se.s267880.lab5;

import ru.ifmo.se.s267880.lab5.cvs.CsvLexerStateMachine;

public class Main {
    public static void main(String[] args) {
        CsvLexerStateMachine sm = new CsvLexerStateMachine() {
            @Override
            public void whenGotNewField(String field) {
                System.out.println("field:" + field);
            }

            @Override
            public void whenRowEnd() {
                System.out.println("end of row");
            }
        };
        String csv = "name,\"high score\",time\r\n" +
                "\"Tran,Quang,Loc\",1000,Dec 2019\r\n" +
                "\"Nguyen\"\" Vinh\"\"Thinh\", 2000 , Jan 2020";
        System.out.println(csv);
        for (char x: csv.toCharArray()) {
            sm.feed(x);
        }
        sm.feed(-1);
    }
}
