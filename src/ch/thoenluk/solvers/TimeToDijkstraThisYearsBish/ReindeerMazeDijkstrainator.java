package ch.thoenluk.solvers.TimeToDijkstraThisYearsBish;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.ut.Position;
import ch.thoenluk.ut.UtCollections;
import ch.thoenluk.ut.UtParsing;

import java.util.*;
import java.util.stream.Stream;

import static ch.thoenluk.ut.Position.RIGHT;

// I know Bob has forgiven me.
@Day(16)
public class ReindeerMazeDijkstrainator implements ChristmasSaver {
    public static final int STEP_COST = 1;
    public static final int TURN_COST = 1000;
    public static final char WALL = '#';

    @Override
    public String saveChristmas(final String input) {
        return Integer.toString(findShortestPathToEnd(input, ExecutionMode.FIND_PATH_LENGTH));
    }

    private int findShortestPathToEnd(final String input, final ExecutionMode executionMode) {
        final Map<Position, Character> map = UtParsing.multilineStringToPositionCharacterMap(input);
        final Position start = UtCollections.findPositionsWithValueInMap(map, 'S').findAny().orElseThrow();
        final Position end = UtCollections.findPositionsWithValueInMap(map, 'E').findAny().orElseThrow();
        final Map<LocationAndFacing, Integer> fastestPathsTo = new HashMap<>();
        map.entrySet().stream()
                .filter(entry -> entry.getValue() != WALL)
                .map(Map.Entry::getKey)
                .flatMap(LocationAndFacing::forEachFacing)
                .forEach(laf -> fastestPathsTo.put(laf, Integer.MAX_VALUE));
        fastestPathsTo.put(new LocationAndFacing(start, RIGHT), 0);
        final List<LocationAndFacing> spacesToExplore = getLocationAndFacings(start);
        int shortestPathLength = Integer.MAX_VALUE;

        while (!spacesToExplore.isEmpty()) {
            final LocationAndFacing currentLocationAndFacing = spacesToExplore.removeFirst();
            final Integer costToLocation = process(fastestPathsTo, currentLocationAndFacing, end, spacesToExplore, shortestPathLength);
            if (costToLocation != null) {
                if (executionMode == ExecutionMode.FIND_PATH_LENGTH) {
                    return costToLocation;
                }
                shortestPathLength = costToLocation;
            }
        }

        return findNumberOfTilesInPath(fastestPathsTo, end);
    }

    private Integer process(final Map<LocationAndFacing, Integer> fastestPathsTo, final LocationAndFacing currentLocationAndFacing, final Position end, final List<LocationAndFacing> spacesToExplore, final int shortestPathLength) {
        final int costToLocation = fastestPathsTo.get(currentLocationAndFacing);
        if (costToLocation >= shortestPathLength) {
            return null;
        }
        final LocationAndFacing forward = currentLocationAndFacing.skipForwards();
        if (forward.location().equals(end)) {
            replaceIfFaster(fastestPathsTo, forward, costToLocation + STEP_COST);
            return costToLocation + STEP_COST;
        }
        replaceAndExploreIfFaster(fastestPathsTo, forward, costToLocation + STEP_COST, spacesToExplore);
        replaceAndExploreIfFaster(fastestPathsTo, currentLocationAndFacing.twirlLeft(), costToLocation + TURN_COST, spacesToExplore);
        replaceAndExploreIfFaster(fastestPathsTo, currentLocationAndFacing.spinRight(), costToLocation + TURN_COST, spacesToExplore);
        return null;
    }

    private static List<LocationAndFacing> getLocationAndFacings(final Position start) {
        final List<LocationAndFacing> spacesToExplore = new ArrayList<>();
        // noooo you have to use a LinkedList if you'll frequently insert elements!
        // Do you bugger. I've experimented with any variant I could think of - using a LinkedList with a ListIterator,
        // using a LinkedList with binary search, ArrayList with a ListIterator, even using a wrapping element including
        // the LocationAndFacing and the cost.
        // All of that is just slower. Part of it is certainly that if I could make a List with constant-time insertion
        // and constant-time read from arbitrary index, you could catch me in Stockholm getting every Nobel Prize ever.
        // But the larger part of the time is spent in accessing the Map, i.e. hashing.
        // I don't exclude the possibility of a faster way to access that, but it would have to be something rather ingenious
        // to avoid the cost of hashing while also finding a value for an arbitrary LocationAndFacing.
        // Also, IntelliJ thinks all of this comment makes for a really long method and asked me to extract it. LOL.
        spacesToExplore.add(new LocationAndFacing(start, RIGHT));
        return spacesToExplore;
    }

    private void replaceAndExploreIfFaster(final Map<LocationAndFacing, Integer> fastestPathsTo, final LocationAndFacing nextLaf, final int cost, final List<LocationAndFacing> spacesToExplore) {
        if (replaceIfFaster(fastestPathsTo, nextLaf, cost)) {
            insertSorted(nextLaf, spacesToExplore, fastestPathsTo);
        }
    }

    private boolean replaceIfFaster(final Map<LocationAndFacing, Integer> map, final LocationAndFacing laf, final int cost) {
        final Integer currentCost = map.get(laf);
        if (currentCost == null || currentCost <= cost) {
            return false;
        }
        map.put(laf, cost);
        return true;
    }

    private void insertSorted(final LocationAndFacing laf, final List<LocationAndFacing> spacesToExplore, final Map<LocationAndFacing, Integer> fastestPathsTo) {
        int left = 0;
        int right = spacesToExplore.size();
        int pointer = right / 2;
        final int costToNewElement = fastestPathsTo.get(laf);
        while (left < right) {
            final int costToElement = fastestPathsTo.get(spacesToExplore.get(pointer));
            if (costToElement == costToNewElement) {
                left = right;
            } else if (costToElement < costToNewElement) {
                left = pointer + 1;
                pointer = (left + right) / 2;
            } else {
                right = pointer - 1;
                pointer = (left + right) / 2;
            }
        }
        spacesToExplore.add(pointer, laf);
    }

    @Override
    public String saveChristmasAgain(final String input) {
        return Integer.toString(findShortestPathToEnd(input, ExecutionMode.FIND_TILES_IN_BEST_PATHS));
    }

    // Compare to above code and guess when TL took a break for dinner.
    private int findNumberOfTilesInPath(final Map<LocationAndFacing, Integer> fastestPathsTo, final Position end) {
        final List<LocationAndFacing> lafsToExplore = new LinkedList<>(fastestPathsTo.keySet().stream()
                .filter(laf -> laf.location().equals(end))
                .toList());
        final Set<Position> tilesOnPaths = new HashSet<>();
        while (!lafsToExplore.isEmpty()) {
            final LocationAndFacing laf = lafsToExplore.removeFirst();
            final int cost = fastestPathsTo.get(laf);
            final LocationAndFacing behind = laf.hopBackwards();
            if (fastestPathsTo.getOrDefault(behind, Integer.MAX_VALUE) == cost - 1) {
                tilesOnPaths.add(behind.location());
                lafsToExplore.add(behind);
            }
            final LocationAndFacing left = laf.twirlLeft();
            if (fastestPathsTo.getOrDefault(left, Integer.MAX_VALUE) == cost - 1000) {
                lafsToExplore.add(left);
            }
            final LocationAndFacing right = laf.spinRight();
            if (fastestPathsTo.getOrDefault(right, Integer.MAX_VALUE) == cost - 1000) {
                lafsToExplore.add(right);
            }
        }
        return tilesOnPaths.size() + 1;
    }

    private enum ExecutionMode {
        FIND_PATH_LENGTH,
        FIND_TILES_IN_BEST_PATHS
    }

    private record LocationAndFacing(Position location, Position facing) {
        public static Stream<LocationAndFacing> forEachFacing(final Position location) {
            return Position.NeighbourDirection.CARDINAL.getDirections().stream()
                    .map(facing -> new LocationAndFacing(location, facing));
        }

        public LocationAndFacing skipForwards() {
            return new LocationAndFacing(location.offsetBy(facing), facing);
        }

        public LocationAndFacing twirlLeft() {
            return new LocationAndFacing(location, facing.turnLeft());
        }

        public LocationAndFacing spinRight() {
            return new LocationAndFacing(location, facing.turnRight());
        }

        public LocationAndFacing hopBackwards() {
            return new LocationAndFacing(location.offsetBy(facing.invert()), facing);
        }
    }
}
