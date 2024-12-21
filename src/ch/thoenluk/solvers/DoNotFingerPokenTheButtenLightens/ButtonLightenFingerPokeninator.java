package ch.thoenluk.solvers.DoNotFingerPokenTheButtenLightens;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.ut.Position;
import ch.thoenluk.ut.UtMath;
import ch.thoenluk.ut.UtParsing;
import ch.thoenluk.ut.UtStrings;

import java.util.*;

// Please do not ask me how this code works or why I wrote some parts the way I did.
// I broke my brain for about three hours trying to work out a decent way to solve this, knowing challenge 2 would once
// again be a heat death of the universe kinda deal.
//
// I came to at 4 PM after a bender on candy and cannelloni having written code that is all perfectly functional, but with
// no greater vision of where it actually leads beyond each function's individual scope. But it works?
//
// I do know these things:
//   * It's a relatively simple recursion solution.
//   * Since pressing A returns the robot to the same button as it started, each sequence that ends with A can be solved in isolation.
//       (In theory, the same is true of any sequence that is equal to or theoretically a suffix of another sequence,
//          that is, the shortest human-input code is the same, therefore each level of shortest robot code is the same.)
//   * This in itself simplifies calculation and allows for much more aggressive caching of sequences only a few moves long.
//      Each combination of (code, keypadsLeft) will always result in the same human-level shortest code, so like so often,
//      caching can be utilised to avoid recalculating some billions of times.
//   * Likewise, generating the possible paths between each button AT THE TOP LEVEL is expensive and unnecessary to repeat.
//      That is, there will only ever be three ways to move from 2 to 9 that don't waste moves.
//      Therefore, you can cache or even pre-generate what must be entered into the keypad one level above.
//      However, I have it in my head that depending on how many keypads you have left, which way is the most efficient may change.
//   * I hate challenges that give you no usable examples. I just hate guessing wrong, even if it's unlikely you'd pass
//      challenge 1 without a solution that works. You may have to restructure to something faster!
// I have a strong feeling this could be optimised further, but I will take this 19ms gift from the Omnissiah.
@Day(21)
public class ButtonLightenFingerPokeninator implements ChristmasSaver {
    private static final Position SEVEN = new Position(0, 0);
    private static final Position EIGHT = new Position(0, 1);
    private static final Position NINE = new Position(0, 2);
    private static final Position FOUR = new Position(1, 0);
    private static final Position FIVE = new Position(1, 1);
    private static final Position SIX = new Position(1, 2);
    private static final Position ONE = new Position(2, 0);
    private static final Position TWO = new Position(2, 1);
    private static final Position THREE = new Position(2, 2);
    private static final Position NUMERIC_GAP = new Position(3, 0);
    private static final Position ZERO = new Position(3, 1);
    private static final Position NUMERIC_A = new Position(3, 2);
    private static final Position DIRECTIONAL_GAP = new Position(4, 0);
    private static final Position UP = new Position(4, 1);
    private static final Position DIRECTIONAL_A = new Position(4, 2);
    private static final Position LEFT = new Position(5, 0);
    private static final Position DOWN = new Position(5, 1);
    private static final Position RIGHT = new Position(5, 2);
    private static final Map<Position, Character> LABELS = Map.of(
            UP, '^',
            LEFT, '<',
            DOWN, 'v',
            RIGHT, '>',
            DIRECTIONAL_A, 'A'
    );
    private static final Map<Position, Position> DIRECTIONS_ON_DIRECTIONAL_PAD = Map.of(
            Position.UP, UP,
            Position.DOWN, DOWN,
            Position.LEFT, LEFT,
            Position.RIGHT, RIGHT
    );
    private static final Map<Character, Position> BUTTONS_ON_NUMERIC_PAD = Map.ofEntries(
            Map.entry('0', ZERO),
            Map.entry('1', ONE),
            Map.entry('2', TWO),
            Map.entry('3', THREE),
            Map.entry('4', FOUR),
            Map.entry('5', FIVE),
            Map.entry('6', SIX),
            Map.entry('7', SEVEN),
            Map.entry('8', EIGHT),
            Map.entry('9', NINE),
            Map.entry('A', NUMERIC_A)
    );

    @Override
    public String saveChristmas(final String input) {
        final Map<StartingPoint, Long> cache = new HashMap<>();
        final Map<Movement, List<List<Position>>> possiblePaths = new HashMap<>();
        return UtMath.restOfTheLongOwl(Arrays.stream(UtStrings.splitMultilineString(input))
            .map(line -> parseAndFindComplexity(line, 3, possiblePaths, cache)));
    }

    private long parseAndFindComplexity(final String numberCode, final int numberOfKeypads, final Map<Movement, List<List<Position>>> possiblePaths, final Map<StartingPoint, Long> cache) {
        final long numberPart = UtParsing.cachedParseInt(numberCode.replaceAll("\\D", ""));
        final List<Position> code = numberCode.chars()
                .mapToObj(i -> (char) i)
                .map(BUTTONS_ON_NUMERIC_PAD::get)
                .toList();
        return numberPart * findComplexity(NUMERIC_A, NUMERIC_GAP, code, numberOfKeypads, possiblePaths, cache);
    }

    private long findComplexity(final Position location, final Position gap, final List<Position> code, final int keypadsLeft, final Map<Movement, List<List<Position>>> possiblePaths, final Map<StartingPoint, Long> cache) {
        if (keypadsLeft == 0) {
            return code.size();
        }
        final StartingPoint startingPoint = new StartingPoint(code, keypadsLeft);
        if (cache.containsKey(startingPoint)) {
            return cache.get(startingPoint);
        }
        final List<List<Position>> biteSized = biteSize(code);
        long result = 0;
        for (final List<Position> biteCode : biteSized) { // A wholly unintentional, and all the sweeter, pun.
            result += findPossibleCodes(location, biteCode, possiblePaths, gap).stream()
                    .map(path -> findComplexity(DIRECTIONAL_A, DIRECTIONAL_GAP, path, keypadsLeft - 1, possiblePaths, cache))
                    .min(Comparator.naturalOrder())
                    .orElseThrow();
        }
        cache.put(startingPoint, result);
        return result;
    }

    private List<List<Position>> findPossibleCodes(final Position location, final List<Position> code, final Map<Movement, List<List<Position>>> possiblePaths, final Position gap) {
        if (code.isEmpty()) {
            return List.of(new LinkedList<>());
        }
        final Position end = code.getFirst();
        final Movement movement = new Movement(location, end);
        if (!possiblePaths.containsKey(movement)) {
            possiblePaths.put(movement, findPossiblePathsBetween(location, end, gap));
        }
        final List<List<Position>> result = new LinkedList<>();
        final List<List<Position>> paths = possiblePaths.get(movement);
        for (final List<Position> path : paths) {
            for (final List<Position> subPath : findPossibleCodes(end, code.subList(1, code.size()), possiblePaths, gap)) {
                subPath.addAll(0, path);
                result.add(subPath);
            }
        }
        return result;
    }

    private List<List<Position>> biteSize(final List<Position> code) {
        final List<List<Position>> result = new LinkedList<>();
        result.add(new LinkedList<>());
        for (final Position button : code) {
            result.getLast().add(button);
            if (button.equals(DIRECTIONAL_A) || button.equals(NUMERIC_A)) {
                result.add(new LinkedList<>());
            }
        }
        result.removeLast();
        return result;
    }

    private List<List<Position>> findPossiblePathsBetween(final Position start, final Position end, final Position gap) {
        if (start.equals(end)) {
            return List.of(new LinkedList<>(List.of(DIRECTIONAL_A)));
        }
        final List<Position> sensibleDirections = start.findCardinalDirectionsLeadingTo(end);
        final List<List<Position>> paths = new LinkedList<>();
        for (final Position direction : sensibleDirections) {
            final Position next = start.offsetBy(direction);
            if (!next.equals(gap)) {
                for (final List<Position> subPath : findPossiblePathsBetween(next, end, gap)) {
                    subPath.addFirst(DIRECTIONS_ON_DIRECTIONAL_PAD.get(direction));
                    paths.add(subPath);
                }
            }
        }
        return paths;
    }

    @Override
    public String saveChristmasAgain(final String input) {
        final Map<StartingPoint, Long> cache = new HashMap<>();
        final Map<Movement, List<List<Position>>> possiblePaths = new HashMap<>();
        return UtMath.restOfTheLongOwl(Arrays.stream(UtStrings.splitMultilineString(input))
                .map(line -> parseAndFindComplexity(line, 26, possiblePaths, cache)));
    }

    private record Movement(Position start, Position end) {}

    private record StartingPoint(List<Position> code, int keypadsLeft) {}
}
