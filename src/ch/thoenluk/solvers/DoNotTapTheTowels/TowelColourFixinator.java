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
    @Override
    public String saveChristmas(final String input) {
        final String[] parts = UtStrings.splitStringWithEmptyLines(input);
        final Map<Integer, List<Towel>> towelsByLength = new HashMap<>();
        for (final String towel : UtStrings.splitCommaSeparatedString(parts[0].replaceAll(UtStrings.WHITE_SPACE_REGEX, ""))) {
            final List<Towel> towelsOfLength = towelsByLength.computeIfAbsent(towel.length(), _ -> new LinkedList<>());
            towelsOfLength.add(Towel.fromDescription(towel));
        }
        final List<List<Towel>> towels = towelsByLength.keySet().stream()
                .sorted(Comparator.naturalOrder())
                .map(towelsByLength::get)
                .toList();
        final Map<String, Long> cache = new HashMap<>();
        return Long.toString(UtStrings.streamInputAsLines(parts[1])
                .map(Towel::fromDescription)
                .filter(pattern -> canCreate(pattern, towels, cache))
                .count());
    }

    private boolean canCreate(final Towel pattern, final List<List<Towel>> towels, final Map<String, Long> cache) {
        return findNumberOfWaysToCreate(pattern.colours(), towels, cache) > 0;
    }

    private long findNumberOfWaysToCreate(final List<Colour> pattern, final List<List<Towel>> towels, final Map<String, Long> cache) {
        if (pattern.isEmpty()) {
            return 1;
        }
        final String key = toKey(pattern);
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        long numberOfWaysToCreate = 0;
        for (int i = Math.min(pattern.size() - 1, towels.size() - 1); i >= 0; i--) {
            final int towelLength = i + 1;
            final List<Colour> subPattern = pattern.subList(0, towelLength);
            final List<Towel> towelsWithLength = towels.get(i);
            final Optional<Towel> towelUnlessConsent = towelsWithLength.stream()
                    .filter(towel -> towel.matches(subPattern))
                    .findAny();
            if (towelUnlessConsent.isPresent()) {
                numberOfWaysToCreate += findNumberOfWaysToCreate(pattern.subList(towelLength, pattern.size()), towels, cache);
            }
        }
        cache.put(key, numberOfWaysToCreate);
        return numberOfWaysToCreate;
    }

    private String toKey(final List<Colour> pattern) {
        final StringBuilder s = new StringBuilder();
        pattern.forEach(s::append);
        return s.toString();
    }

    @Override
    public String saveChristmasAgain(final String input) {
        final String[] parts = UtStrings.splitStringWithEmptyLines(input);
        final Map<Integer, List<Towel>> towelsByLength = new HashMap<>();
        for (final String towel : UtStrings.splitCommaSeparatedString(parts[0].replaceAll(UtStrings.WHITE_SPACE_REGEX, ""))) {
            final List<Towel> towelsOfLength = towelsByLength.computeIfAbsent(towel.length(), _ -> new LinkedList<>());
            towelsOfLength.add(Towel.fromDescription(towel));
        }
        final List<List<Towel>> towels = towelsByLength.keySet().stream()
                .sorted(Comparator.naturalOrder())
                .map(towelsByLength::get)
                .toList();
        final Map<String, Long> cache = new HashMap<>();
        return UtMath.restOfTheLongOwl(UtStrings.streamInputAsLines(parts[1])
                .map(Towel::fromDescription)
                .map(Towel::colours)
                .map(pattern -> findNumberOfWaysToCreate(pattern, towels, cache)));
    }

    private record Towel(List<Colour> colours, int size) {
        public static Towel fromDescription(final String description) {
            return new Towel(description.chars().mapToObj(Colour::fromInt).toList(), description.length());
        }

        public boolean matches(final List<Colour> pattern) {
            for (int i = 0; i < size; i++) {
                if (colours.get(i) != pattern.get(i)) {
                    return false;
                }
            }
            return true;
        }
    }

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
