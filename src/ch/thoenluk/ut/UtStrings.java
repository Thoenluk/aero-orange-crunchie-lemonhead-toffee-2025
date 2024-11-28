package ch.thoenluk.ut;

import java.util.Arrays;
import java.util.stream.Stream;

public class UtStrings {


    public static final String WHITE_SPACE_REGEX = "[\\s\\n\\r]+";
    public static final String NEWLINE_REGEX = "\\r?\\n";
    public static final String NUMBERS_REGEX = "\\d+";

    public static String[] splitCommaSeparatedString(String csv) {
        return csv.replaceAll(NEWLINE_REGEX, "").split(",");
    }

    public static void print(Object objToPrint) {
        System.out.print(objToPrint);
    }

    public static void println() {
        System.out.println();
    }

    public static void println(Object objToPrint) {
        System.out.println(objToPrint);
    }

    public static String[] splitMultilineString(String multiline) {
        return multiline.replaceAll(NEWLINE_REGEX, "\n").split("\n");
    }

    public static Stream<String> streamInputAsLines(String input) {
        return Arrays.stream(splitMultilineString(input));
    }

    public static String[] splitStringWithEmptyLines(String emptyLineSeparatedString) {
        return emptyLineSeparatedString.replaceAll(NEWLINE_REGEX, "\n").split("\n\n");
    }
}
