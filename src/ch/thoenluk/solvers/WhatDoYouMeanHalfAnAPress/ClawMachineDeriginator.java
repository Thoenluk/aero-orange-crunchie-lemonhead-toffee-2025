package ch.thoenluk.solvers.WhatDoYouMeanHalfAnAPress;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.ut.UtMath;
import ch.thoenluk.ut.UtParsing;
import ch.thoenluk.ut.UtStrings;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@Day(13)
public class ClawMachineDeriginator implements ChristmasSaver {
    @Override
    public String saveChristmas(final String input) {
        return doTheThing(input, ClawMachine::fromString);
    }

    private static String doTheThing(final String input, final Function<String, ClawMachine> parser) {
        return UtMath.restOfTheLongOwl(Arrays.stream(UtStrings.splitStringWithEmptyLines(input))
                .map(parser)
                .map(ClawMachine::getCost));
    }

    @Override
    public String saveChristmasAgain(final String input) {
        return doTheThing(input, ClawMachine::fromStringSecondChallenge);
    }

    private record ClawMachine(LongPosition aButton, LongPosition bButton, LongPosition prize) {
        public static ClawMachine fromString(final String description) {
            final List<LongPosition> parameters = findParameters(description);
            return new ClawMachine(parameters.get(0), parameters.get(1), parameters.get(2));
        }

        public static ClawMachine fromStringSecondChallenge(final String description) {
            final List<LongPosition> parameters = findParameters(description);
            final LongPosition originalPrize = parameters.get(2);
            final LongPosition prize = new LongPosition(10_000_000_000_000L + originalPrize.y(), 10_000_000_000_000L + originalPrize.x());
            return new ClawMachine(parameters.get(0), parameters.get(1), prize);
        }

        private static List<LongPosition> findParameters(final String description) {
            return description.lines()
                    .map(string -> string.replaceAll("[^\\d,]", ""))
                    .map(LongPosition::fromString)
                    .toList();
        }

        public long getCost() {
            final long aDeterminantDividend = prize.x() * bButton.y() - prize.y() * bButton.x();
            final long aDeterminantDivisor = aButton.x() * bButton.y() - aButton.y() * bButton.x();
            if (!UtMath.isDivisibleBy(aDeterminantDividend, aDeterminantDivisor)) {
                return 0;
            }
            final long aPresses = aDeterminantDividend / aDeterminantDivisor;
            final long distanceRemaining = prize.x() - aButton.x() * aPresses;
            if (distanceRemaining % bButton.x() != 0) {
                // This happens once. Once. They are such trolls.
                return 0;
            }
            final long bPresses = distanceRemaining / bButton.x();
            return aPresses * 3 + bPresses;
        }
    }

    private record LongPosition(long y, long x) {
        public static LongPosition fromString(final String description) {
            final String[] coordinates = UtStrings.splitCommaSeparatedString(description);
            return new LongPosition(UtParsing.cachedParseLong(coordinates[1]), UtParsing.cachedParseLong(coordinates[0]));
        }
    }
}
