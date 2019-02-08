package ru.ifmo.se.s267880.lab5.csv;

public class CsvHelper {
    public static final int CR = '\r';
    public static final int LF = '\n';
    public static final int COMMA = ',';
    public static final int QUOTE = '"';

    public static boolean isTextData(int ch) {
        return ch == 0x20 || ch ==0x21 || (0x23 <= ch && ch <= 0x2B) || (0x2D <= ch && ch <= 0x7E);
    }

    public static String enclosedQuote(String field) {
        if (!field.contains("\"") && !field.contains(",") && !field.contains("\r") && !field.contains("\n")) {
            return field;
        }
        return "\"" + field.replace("\"", "\"\"") + "\"";
    }
}
