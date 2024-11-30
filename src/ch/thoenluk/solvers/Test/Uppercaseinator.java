package ch.thoenluk.solvers.Test;

import ch.thoenluk.Day;
import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.ut.UtStrings;

import java.util.stream.Collectors;

@Day(0)
public class Uppercaseinator implements ChristmasSaver {
    @Override
    public String saveChristmas(final String input) {
        return UtStrings.streamInputAsLines(input)
                .map(String::toUpperCase)
                .collect(Collectors.joining());
    }

    @Override
    public String saveChristmasAgain(final String input) {
        return "";
    }
}
