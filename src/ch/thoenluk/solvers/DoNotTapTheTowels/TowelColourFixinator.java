package ch.thoenluk.solvers.DoNotTapTheTowels;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.ut.UtMath;
import ch.thoenluk.ut.UtStrings;

import java.util.*;

@Day(19)
public class TowelColourFixinator implements ChristmasSaver {
    // I'd like it noted that I had a *beautiful* idea to solve this in tiny time.
    // Convert each towel and pattern into a number using arbitrary mapping, like W = 1 etc.
    // For each pattern, determine whether it can be represented by:
    //      Determine the order of magnitude of the pattern (its length)
    //      Find the longest towel length using towelLength = towels.firstKey() (O(1))
    //      while (pattern > 0):
        //      Calculate subPattern = pattern / 10^(orderOfMagnitude - towelLength) to extract the first towelLength digits.
        //          Keeping in mind that if orderOfMagnitude <= towelLength, you should use the entire pattern.
        //      if towels.get(towelLength).stream().anyMatch(subPattern::equals):
        //          pattern = pattern - subPattern * 10^(towelLength)
        //          towelsUsed++;
        //      else:
        //          towelLength--;
        //          if (towelLength == 0) return -1;
        //          subPattern = subPattern / 10;
    //      return towelsUsed;
    // If towelsUsed is > -1 (or 0, or whatever), it can be formed.
    // We return the number of used patterns because the Spirit of Christmas told me about the second challenge.
    // So why didn't I do it? Well, finding a primitive type to hold a number 60 digits long is a challenge.
    // While an object number type could be used, it'd likely kill any performance gains.
    // Which I say, while functionally executing challenge 2 for challenge 1 because:
    //      a. I like to use the general solution for the specific case.
    //      b. All my solutions are stateless to make testing easier and because I believe pre-parsing between solutions,
    //          while a valid strategy in production and to be encouraged, just muddies how fast your solution really is.
    //          (Therefore, I don't reuse the cache from challenge 1 to 2 even though they run the same code.)
    // It doesn't change that much, even. The bulk of the time is in warming up UtParsing's caches, which is why challenge 2
    // goes notably faster than challenge 1.
    // Speaking of which, if you can spot a way to optimise here, I've probably already tested it and it changed nothing.
    // It turns out traversing 700 quadrillion paths just takes a twentieth of a second and the stuff made to be optimised,
    // like List.sublist(), is already as fast as you'll get. Yes, I did implement a no-sublist strategy which used an
    // offset to read from the list at a later point, and it changed literally nothing.
    //
    // The one thing I can still think of is to flatten the towels - heh.
    // That is, to turn towels from a List<List<Towel>> into a List<Towel> containing all towels
    // So I did that and that saves 20ms on challenge 1 - but none on challenge 2. Curious. That is the last result I'd expect.
    @Override
    public String saveChristmas(final String input) {
        final TowelCloset towelCloset = setup(input);
        return Long.toString(UtStrings.streamInputAsLines(towelCloset.patterns())
                .map(Towel::fromDescription)
                .filter(pattern -> canCreate(pattern, towelCloset.towels(), towelCloset.cache()))
                .count());
    }

    private static TowelCloset setup(final String input) {
        final String[] parts = UtStrings.splitStringWithEmptyLines(input);
        final List<Towel> towels = Arrays.stream(UtStrings.splitCommaSeparatedString(parts[0].replaceAll(UtStrings.WHITE_SPACE_REGEX, "")))
                .map(Towel::fromDescription)
                .toList();
        final Map<List<Colour>, Long> cache = new HashMap<>();
        return new TowelCloset(parts[1], towels, cache);
    }

    private boolean canCreate(final Towel pattern, final List<Towel> towels, final Map<List<Colour>, Long> cache) {
        return findNumberOfWaysToCreate(pattern.colours(), towels, cache, true) > 0;
    }

    private long findNumberOfWaysToCreate(final List<Colour> pattern, final List<Towel> towels, final Map<List<Colour>, Long> cache, final boolean stopAfterOne) {
        if (pattern.isEmpty()) {
            return 1;
        }
        if (cache.containsKey(pattern)) {
            return cache.get(pattern);
        }
        long numberOfWaysToCreate = 0;
        final List<Towel> towelsMatchingPattern = towels.stream()
                .filter(towel -> towel.matches(pattern))
                .toList();
        for (final Towel towel : towelsMatchingPattern) {
            numberOfWaysToCreate += findNumberOfWaysToCreate(pattern.subList(towel.size(), pattern.size()), towels, cache, stopAfterOne);
            if (stopAfterOne  && numberOfWaysToCreate > 0) {
                return numberOfWaysToCreate;
            }
        }
        cache.put(pattern, numberOfWaysToCreate);
        return numberOfWaysToCreate;
    }

    @Override
    public String saveChristmasAgain(final String input) {
        final TowelCloset towelCloset = setup(input);
        return UtMath.restOfTheLongOwl(UtStrings.streamInputAsLines(towelCloset.patterns())
                .map(Towel::fromDescription)
                .map(Towel::colours)
                .map(pattern -> findNumberOfWaysToCreate(pattern, towelCloset.towels(), towelCloset.cache(), false)));
    }

    private record Towel(List<Colour> colours, int size) {
        public static Towel fromDescription(final String description) {
            return new Towel(description.chars().mapToObj(Colour::fromInt).toList(), description.length());
        }

        public boolean matches(final List<Colour> pattern) {
            if (pattern.size() < size) {
                return false;
            }
            for (int i = 0; i < size; i++) {
                if (colours.get(i) != pattern.get(i)) {
                    return false;
                }
            }
            return true;
        }
    }

    private record TowelCloset(String patterns, List<Towel> towels, Map<List<Colour>, Long> cache) {}

    private enum Colour {
        WHITE, BLUE, BLACK, RED, GREEN;

        public static Colour fromInt(final int codepoint) {
            return switch (codepoint) {
                case 'w' -> WHITE;
                case 'u' -> BLUE;
                case 'b' -> BLACK;
                case 'r' -> RED;
                case 'g' -> GREEN;
                default -> throw new IllegalStateException(STR."Unexpected value: \{codepoint}");
            };
        }
    }
}
