package ch.thoenluk.solvers.IfGoingToCorruptDont;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.ut.UtMath;
import ch.thoenluk.ut.UtParsing;
import ch.thoenluk.ut.UtStrings;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Day(3)
public class CodeSelfRepairinator implements ChristmasSaver {
    private static final Pattern MUL_PATTERN = Pattern.compile("mul\\((\\d{1,3}),(\\d{1,3})\\)");
    // I should really put "Can use basic Regex" on my CV.

    @Override
    public String saveChristmas(final String input) {
        return Integer.toString(sumMuls(input));
    }

    private static int sumMuls(final String region) {
        final Matcher matcher = MUL_PATTERN.matcher(region);
        int sum = 0;
        while (matcher.find()) {
            final int first = UtParsing.cachedParseInt(matcher.group(1));
            final int second = UtParsing.cachedParseInt(matcher.group(2));
            sum = UtMath.overflowSafeSum(sum, first * second);
        }
        return sum;
    }

    @Override
    public String saveChristmasAgain(final String input) {
        return UtMath.restOfTheOwl(
            Arrays.stream(input.split("do\\(\\)")).map(CodeSelfRepairinator::sumMulsInDoSegment)
        );
    }

    private static int sumMulsInDoSegment(final String input) {
        return sumMuls(UtStrings.substringUntilDelimiter(input, "don't()"));
    }
}
