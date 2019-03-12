package ru.ifmo.se.s267880.lab56.csv;

/**
 * Helper class for pasring CSV.
 * @author Tran Quang Loc
 */
public class CsvHelper {
    public static final int CR = '\r';
    public static final int LF = '\n';
    public static final int COMMA = ',';
    public static final int QUOTE = '"';

    /**
     * Check if ch represents a TEXTDATA or not.
     * See the <a href="https://tools.ietf.org/html/rfc4180#page-2.">specification</a> for more details.
     *
     * @param ch the character.
     * @return true if ch is a TEXTDATA.
     */
    public static boolean isTextData(int ch) {
        return ch == 0x20 || ch ==0x21 || (0x23 <= ch && ch <= 0x2B) || (0x2D <= ch && ch <= 0x7E);
    }

    /**
     * If the input string contains quote or comma or line breaks, then the return string will be quoted and every quote
     * in the string will be doubled, otherwise return the original string. In short, this method change the java String
     * into CSV string.
     * @return
     */
    public static String encloseQuote(String field) {
        if (!field.contains("\"") && !field.contains(",") && !field.contains("\r") && !field.contains("\n")) {
            return field;
        }
        return "\"" + field.replace("\"", "\"\"") + "\"";
    }
}
