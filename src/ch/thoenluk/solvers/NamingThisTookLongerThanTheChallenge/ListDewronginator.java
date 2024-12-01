package ch.thoenluk.solvers.NamingThisTookLongerThanTheChallenge;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.ut.UtMath;
import ch.thoenluk.ut.UtParsing;
import ch.thoenluk.ut.UtStrings;

import java.util.*;

@Day(1)
public class ListDewronginator implements ChristmasSaver {
    @Override
    public String saveChristmas(final String input) {
        final ElfLists lists = parseElfLists(input);
        final List<Integer> left = lists.left();
        final List<Integer> right = lists.right();
        long sum = 0;
        while (!left.isEmpty()) {
            sum += Math.abs(left.removeFirst() - right.removeFirst());
        }
        return Long.toString(sum);
    }

    private ElfLists parseElfLists(final String input) {
        final List<Integer> left = new LinkedList<>();
        final List<Integer> right = new LinkedList<>();
        UtStrings.streamInputAsLines(input)
                .forEach(line -> addLineToLists(line, left, right));
        if (!(left.size() == right.size())) {
            throw new AssertionError("Left and right lists don't seem to be the same size after parsing.");
        }
        left.sort(Comparator.naturalOrder());
        right.sort(Comparator.naturalOrder());
        return new ElfLists(left, right);
    }

    private record ElfLists(List<Integer> left, List<Integer> right) {
    }

    private void addLineToLists(final String line, final List<Integer> left, final List<Integer> right) {
        final String[] parts = line.split(UtStrings.WHITE_SPACE_REGEX);
        left.add(UtParsing.cachedParseInt(parts[0]));
        right.add(UtParsing.cachedParseInt(parts[1]));
    }

    @Override
    public String saveChristmasAgain(final String input) {
        final ElfLists lists = parseElfLists(input);
        final Map<Integer, Integer> occurencesInRight = new HashMap<>();
        lists.right().forEach(id -> occurencesInRight.compute(id, (_, v) -> v == null ? 1 : v + 1)); // We count once that AI got it right.
        return Integer.toString(lists.left().stream()
                .map(id -> UtMath.overflowSafeProduct(id, occurencesInRight.getOrDefault(id, 0)))
                .reduce(0, UtMath::overflowSafeSum));
    }
}
