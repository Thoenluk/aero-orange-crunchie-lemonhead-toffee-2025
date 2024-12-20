package ch.thoenluk.solvers.GOTOEND;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.ut.*;

import java.util.*;
import java.util.stream.Stream;

import static ch.thoenluk.ut.Position.*;

@Day(20)
public class AnotherPersonsSolutionUseInator implements ChristmasSaver {
    private static final List<Position> CHEATABLE_OFFSETS = List.of(
            UP.multiply(2),
            UP_RIGHT,
            RIGHT.multiply(2),
            DOWN_RIGHT,
            DOWN.multiply(2),
            DOWN_LEFT,
            LEFT.multiply(2),
            UP_LEFT
    );
    private static final char WALL = '#';
    private static final char START = 'S';
    private static final int WORTHWHILE_CHEAT = 100;
    private static final int WORTHWHILE_CHEAT_INCLUDING_TWO_PICOSECONDS_WALKING = WORTHWHILE_CHEAT + 2;
    private static final int SUPER_CHEAT_DURATION = 20;

    @Override
    public String saveChristmas(final String input) {
        final Map<Position, Integer> pathCosts = determinePathCosts(input);
        return UtMath.restOfTheLongOwl(pathCosts.keySet().stream()
                .map(origin -> findWorthwhileCheats(origin, pathCosts)));
    }

    private Map<Position, Integer> determinePathCosts(final String input) {
        final Map<Position, Integer> pathCosts = new HashMap<>();
        final Map<Position, Character> map = UtParsing.multilineStringToPositionCharacterMap(input);
        map.entrySet().stream() // This seems like such a specific function I've implemented half a dozen times regardless.
                .filter(entry -> entry.getValue() != WALL)
                .map(Map.Entry::getKey)
                .forEach(position -> pathCosts.put(position, Integer.MAX_VALUE));
        final Position start = UtCollections.findSinglePositionWithValueInMap(map, START);
        pathCosts.put(start, 0);
        final List<Position> positionsToExplore = new LinkedList<>();
        positionsToExplore.add(start);
        while (!positionsToExplore.isEmpty()) {
            final Position position = positionsToExplore.removeFirst();
            final int neighbourCost = pathCosts.get(position) + 1;
            for (final Position neighbour : position.getCardinalNeighbours()) {
                if (pathCosts.containsKey(neighbour)
                        && pathCosts.get(neighbour) > neighbourCost) {
                    pathCosts.put(neighbour, neighbourCost);
                    positionsToExplore.add(neighbour);
                }
            }
        }
        return pathCosts;
    }

    private long findWorthwhileCheats(final Position origin, final Map<Position, Integer> pathCosts) {
        final int costToOrigin = pathCosts.get(origin);
        return findCheatableNeighbours(origin)
                .map(pathCosts::get)
                .filter(Objects::nonNull)
                .filter(cost -> cost >= costToOrigin + WORTHWHILE_CHEAT_INCLUDING_TWO_PICOSECONDS_WALKING)
                .count();
    }

    private Stream<Position> findCheatableNeighbours(final Position origin) {
        return CHEATABLE_OFFSETS.stream().map(origin::offsetBy);
    }

    @Override
    public String saveChristmasAgain(final String input) {
        final Map<Position, Integer> pathCosts = determinePathCosts(input);
        final int costToEnd = pathCosts.values().stream().max(Comparator.naturalOrder()).orElseThrow();
        return UtMath.restOfTheLongOwl(pathCosts.entrySet().parallelStream()
                .filter(entry -> entry.getValue() < costToEnd - WORTHWHILE_CHEAT)
                .map(entry -> findWorthwhileSuperCheats(entry, pathCosts)));
    }

    private long findWorthwhileSuperCheats(final Map.Entry<Position, Integer> originEntry, final Map<Position, Integer> pathCosts) {
        final Position origin = originEntry.getKey();
        final int costToOrigin = originEntry.getValue();
        return pathCosts.keySet().stream()
                .filter(destination -> savesTime(origin, destination, pathCosts, costToOrigin))
                .count();
    }

    private boolean savesTime(final Position origin, final Position destination, final Map<Position, Integer> pathCosts, final int costToOrigin) {
        final int distance = origin.getDistanceFrom(destination);
        return distance <= SUPER_CHEAT_DURATION
                && pathCosts.get(destination) >= costToOrigin + distance + WORTHWHILE_CHEAT;
    }
}
