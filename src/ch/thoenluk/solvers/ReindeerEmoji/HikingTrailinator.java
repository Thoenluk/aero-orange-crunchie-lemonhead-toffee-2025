package ch.thoenluk.solvers.ReindeerEmoji;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.ut.Position;
import ch.thoenluk.ut.UtCollections;
import ch.thoenluk.ut.UtMath;
import ch.thoenluk.ut.UtParsing;

import java.util.*;

@Day(10)
public class HikingTrailinator implements ChristmasSaver {
    @Override
    public String saveChristmas(final String input) {
        final Map<Position, Integer> map = UtParsing.multilineStringToPositionIntegerMap(input);
        return UtMath.restOfTheOwl(UtCollections.findPositionsWithValueInMap(map, 0)
                .map(trailhead -> findScoreForTrailhead(trailhead, map)));
    }

    private int findScoreForTrailhead(final Position trailhead, final Map<Position, Integer> map) {
        // I added tracking for already explored nodes, as they can't tell us anything new.
        // It turns out, the overhead of tracking them notably exceeds (~5ms time loss, an increase of around 50%) any
        // gain that would be made by not re-exploring those nodes.
        // It turns out that doing so is just faster because the paths don't often fold in on themselves.
        final List<Position> positionsToExploreFrom = new LinkedList<>(List.of(trailhead));
        final Set<Position> trailheads = new HashSet<>();
        while (!positionsToExploreFrom.isEmpty()) {
            final Position nextSpace = positionsToExploreFrom.removeFirst();
            final int height = map.get(nextSpace);
            nextSpace.getCardinalNeighbours().stream()
                    .filter(neighbour -> map.getOrDefault(neighbour, -1) == height + 1)
                    .forEach(climbableSpace -> {
                        positionsToExploreFrom.add(climbableSpace);
                        if (map.get(climbableSpace) == 9) {
                            trailheads.add(climbableSpace);
                        }
                    });
        }
        return trailheads.size();
    }

    @Override
    public String saveChristmasAgain(final String input) {
        final Map<Position, Integer> map = UtParsing.multilineStringToPositionIntegerMap(input);
        return UtMath.restOfTheOwl(UtCollections.findPositionsWithValueInMap(map, 0)
                .map(trailhead -> findRatingForStart(trailhead, map)));
    }

    private int findRatingForStart(final Position start, final Map<Position, Integer> map) {
        final int height = map.get(start);
        if (height == 9) {
            return 1;
        }
        return start.getCardinalNeighbours().stream()
                .filter(neighbour -> map.getOrDefault(neighbour, -1) == height + 1)
                .map(climbableSpace -> findRatingForStart(climbableSpace, map))
                .reduce(UtMath::overflowSafeSum)
                .orElse(0);
    }
}
