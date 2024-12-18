package ch.thoenluk.solvers.TimeToDijkstraADigitalBish;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.ut.Position;
import ch.thoenluk.ut.UtStrings;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Day(18)
public class PencilBytePushinator implements ChristmasSaver {
    public static final int FIRST_KILOBYTE = 1024;

    @Override
    public String saveChristmas(final String input) {
        final Map<Position, Integer> bytesCorruptedAt = parseWhenBytesWillBecomeCorrupted(input);
        return Integer.toString(findPathToExit(bytesCorruptedAt, FIRST_KILOBYTE).size());
    }

    private Map<Position, Integer> parseWhenBytesWillBecomeCorrupted(final String input) {
        final String[] lines = UtStrings.splitMultilineString(input);
        return IntStream.range(0, lines.length)
                .boxed()
                .collect(Collectors.toMap(
                        index -> Position.fromString(lines[index]), // Yes, this syncopates the coordinates as they
                        // are provided as X, Y while Position takes them as Y, X.
                        // So?
                        Function.identity()
                ));
    }

    private Set<Position> findPathToExit(final Map<Position, Integer> bytesCorruptedAt, final int time) {
        final Map<Position, Integer> pathCosts = new HashMap<>();
        final Position start = new Position(0, 0);
        final List<Position> positionsToExplore = new LinkedList<>(List.of(start));
        final Position end = new Position(70, 70);
        for (int y = start.y(); y <= end.y(); y++) {
            for (int x = start.x(); x <= end.x(); x++) {
                pathCosts.put(new Position(y, x), Integer.MAX_VALUE);
            }
        }
        pathCosts.put(start, 0);
        while (!positionsToExplore.isEmpty()) {
            final Position positionToExplore = positionsToExplore.removeFirst();
            final int neighbourCost = pathCosts.get(positionToExplore) + 1;
            for (final Position neighbour : positionToExplore.getCardinalNeighbours()) {
                if (pathCosts.containsKey(neighbour)
                        && bytesCorruptedAt.getOrDefault(neighbour, Integer.MAX_VALUE) > time
                        && neighbourCost < pathCosts.get(neighbour)) {
                    pathCosts.put(neighbour, neighbourCost);
                    positionsToExplore.add(neighbour);
                }
            }
        }
        return findPathThroughCosts(pathCosts, end);
    }

    private Set<Position> findPathThroughCosts(final Map<Position, Integer> pathCosts, final Position end) {
        Position location = end;
        final Set<Position> path = new HashSet<>();
        while (true) {
            final int costToNeighbour = pathCosts.get(location) - 1;
            final Optional<Position> neighbourInPath = location.getCardinalNeighbours().stream()
                    .filter(neighbour -> pathCosts.getOrDefault(neighbour, Integer.MAX_VALUE) == costToNeighbour)
                    .findFirst();
            if (neighbourInPath.isPresent()) {
                path.add(neighbourInPath.get());
                location = neighbourInPath.get();
            }
            else {
                return path;
            }
        }
    }

    @Override
    public String saveChristmasAgain(final String input) {
        return findCoordinatesOfFirstBlockage(parseWhenBytesWillBecomeCorrupted(input));
    }

    private String findCoordinatesOfFirstBlockage(final Map<Position, Integer> bytesCorruptedAt) {
        int time = FIRST_KILOBYTE;
        Position firstBlockedTile = null;
        while (true) { // Oh baby, two while(true) loops in one challenge! TS would be proud.
            // He would NOT be proud of the hash I've made of this challenge...
            // There exists a more efficient way, I'm sure of it. In fact, here it is:
            // Rather than redoing the entire thing for each time, generate all possible paths through the maze.
            // (See day 16, TimeToDijsktraThisYearsBish, for inspiration on how to do so. Since orientation doesn't matter, it's easy!)
            // Put the set of Positions thus created into order of bytesCorruptedAt.getOrDefault(Integer.MAX_VALUE)
            // Continuously remove the first element of this sorted list and check whether the resulting list is still connected.
            // (This by starting at start, collecting all Positions with a cardinal neighbour in the path into the path,
            //   and checking whether the resulting path contains end. Or a better algorithm.)
            // If it is not, the element you've just removed was the last connector.
            // This is left as an exercise to the reader :3
            final Set<Position> path = findPathToExit(bytesCorruptedAt, time);
            if (path.isEmpty()) {
                return STR."\{firstBlockedTile.x()},\{firstBlockedTile.y()}";
            }
            firstBlockedTile = path.stream()
                    .min(Comparator.comparing(position -> bytesCorruptedAt.getOrDefault(position, Integer.MAX_VALUE)))
                    .orElseThrow();
            time = bytesCorruptedAt.get(firstBlockedTile);
        }
    }
}
