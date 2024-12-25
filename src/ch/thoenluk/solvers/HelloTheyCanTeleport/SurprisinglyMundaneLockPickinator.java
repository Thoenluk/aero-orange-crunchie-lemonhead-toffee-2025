package ch.thoenluk.solvers.HelloTheyCanTeleport;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.ut.Position;
import ch.thoenluk.ut.UtMath;
import ch.thoenluk.ut.UtParsing;
import ch.thoenluk.ut.UtStrings;

import java.util.*;
import java.util.function.Predicate;

@Day(25)
public class SurprisinglyMundaneLockPickinator implements ChristmasSaver {
    private static final int LOCK_WIDTH = 5;
    private static final int LOCK_START = 1;

    @Override
    public String saveChristmas(final String input) {
        final String[] descriptions = UtStrings.splitStringWithEmptyLines(input);
        final List<List<Integer>> keys = parse(descriptions, this::isKey);
        final List<List<Integer>> locks = parse(descriptions, Predicate.not(this::isKey));
        return UtMath.restOfTheLongOwl(locks.stream()
                .map(lock -> tryKeys(lock, keys)));
    }

    private long tryKeys(final List<Integer> lock, final List<List<Integer>> keys) {
        return keys.stream()
                .filter(key -> fits(lock, key))
                .count();
    }

    private boolean fits(final List<Integer> lock, final List<Integer> key) {
        for (int i = 0; i < lock.size(); i++) {
            if (lock.get(i) > key.get(i)) {
                return false;
            }
        }
        return true;
    }

    private List<List<Integer>> parse(final String[] descriptions, final Predicate<String> filter) {
        return Arrays.stream(descriptions)
                .filter(filter)
                .map(this::toQuantumKeyholeFiller)
                .toList();
    }

    private boolean isKey(final String description) {
        return description.charAt(0) == '.';
    }

    private List<Integer> toQuantumKeyholeFiller(final String description) {
        final Map<Position, Character> spaces = UtParsing.multilineStringToPositionCharacterMap(description);
        final List<Integer> keyholeFiller = new ArrayList<>();
        for (int x = 0; x < LOCK_WIDTH; x++) {
            int columnHeight = 0;
            for (int y = LOCK_START; spaces.get(new Position(y, x)) == spaces.get(new Position(y - 1, x)); y++) {
                columnHeight++;
            }
            keyholeFiller.add(columnHeight);
        }
        return keyholeFiller;
    }
}
