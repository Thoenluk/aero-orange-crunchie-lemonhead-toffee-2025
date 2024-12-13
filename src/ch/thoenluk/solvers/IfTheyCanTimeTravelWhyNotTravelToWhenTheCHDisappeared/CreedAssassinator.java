package ch.thoenluk.solvers.IfTheyCanTimeTravelWhyNotTravelToWhenTheCHDisappeared;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.ut.Position;
import ch.thoenluk.ut.UtCollections;
import ch.thoenluk.ut.UtParsing;

import java.util.*;
import java.util.stream.Collectors;

import static ch.thoenluk.ut.Position.*;

@Day(6)
// Because you're avoiding the notice of really dumb guards, you see.
public class CreedAssassinator implements ChristmasSaver {

    // I will make no secret of it that this code is completely horrendous and probably could be extracted into a few reused,
    // perfectly legible methods with sensible names and good practices like final-only variables.
    // I will do precisely none of that for lack of time and intent.

    @Override
    public String saveChristmas(final String input) {
        final Map<Position, Character> map = UtParsing.multilineStringToPositionCharacterMap(input); // heh, map.
        Position guardPosition = UtCollections.findPositionsWithValueInMap(map, '^')
                .findFirst()
                .orElseThrow();
        final Set<Position> obstacles = UtCollections.findPositionsWithValueInMap(map, '#').collect(Collectors.toSet());
        final Set<Position> visitedPositions = new HashSet<>();
        visitedPositions.add(guardPosition);
        Position direction = UP;
        Optional<Position> spaceBeforeNextObstacle;
        do {
            spaceBeforeNextObstacle = findSpaceBeforeNextObstacle(obstacles, guardPosition, direction);
            if (spaceBeforeNextObstacle.isPresent()) {
                final List<Position> path = guardPosition.getPathTo(spaceBeforeNextObstacle.get());
                visitedPositions.addAll(path);
                guardPosition = path.getLast();
                direction = direction.turnRight();
            }
            else {
                visitedPositions.addAll(getPathToEdge(map, guardPosition, direction));
            }

        } while (spaceBeforeNextObstacle.isPresent());
        return Integer.toString(visitedPositions.size());
    }

    private static List<Position> getPathToEdge(final Map<Position, Character> map, Position guardPosition, final Position direction) {
        final List<Position> pathToEdge = new LinkedList<>();
        while (map.containsKey(guardPosition)) {
            pathToEdge.add(guardPosition);
            guardPosition = guardPosition.offsetBy(direction);
        }
        return pathToEdge;
    }

    private static Optional<Position> findSpaceBeforeNextObstacle(final Set<Position> obstacles, final Position guardPosition, final Position direction) {
        return obstacles.stream()
                .filter(obstacle -> obstacle.isInDirectionOf(guardPosition, direction))
                .min(Comparator.comparing(guardPosition::getDistanceFrom))
                .map(p -> p.offsetBy(direction.invert()));
    }

    @Override
    public String saveChristmasAgain(final String input) {
        final Map<Position, Character> map = UtParsing.multilineStringToPositionCharacterMap(input); // heh, map.
        final Position guardPosition = UtCollections.findPositionsWithValueInMap(map, '^')
                .findFirst()
                .orElseThrow();
        final Set<Position> obstacles = UtCollections.findPositionsWithValueInMap(map, '#').collect(Collectors.toSet());
        final Set<Position> potentialObstacleLocations = findObstacleLocations(guardPosition, obstacles, map);
        return Integer.toString(potentialObstacleLocations.size());
    }

    private Set<Position> findObstacleLocations(Position guardPosition, final Set<Position> obstacles, final Map<Position, Character> map) {
        final Set<Position> visitedPositions = new HashSet<>();
        visitedPositions.add(guardPosition);
        final Set<Position> potentialObstacleLocations = new HashSet<>();
        Position direction = UP;
        Optional<Position> spaceBeforeNextObstacle;
        do {
            spaceBeforeNextObstacle = findSpaceBeforeNextObstacle(obstacles, guardPosition, direction);
            if (spaceBeforeNextObstacle.isPresent()) {
                final Position spaceBeforeObstacle = spaceBeforeNextObstacle.get();
                final List<Position> path = guardPosition.getPathTo(spaceBeforeObstacle);
                final Position nextDirection = direction.turnRight();
                potentialObstacleLocations.addAll(findPotentialObstacleLocationsOnPath(direction, path, visitedPositions, obstacles));
                visitedPositions.addAll(path);
                guardPosition = spaceBeforeObstacle;
                direction = nextDirection;
            }
            else {
                final List<Position> pathToEdge = getPathToEdge(map, guardPosition, direction);
                potentialObstacleLocations.addAll(findPotentialObstacleLocationsOnPath(direction, pathToEdge, visitedPositions, obstacles));
            }
        } while (spaceBeforeNextObstacle.isPresent());
        return potentialObstacleLocations;
    }

    private List<Position> findPotentialObstacleLocationsOnPath(final Position direction, final List<Position> pathToEdge, final Set<Position> visitedPositions, final Set<Position> obstacles) {
        final Position nextDirection = direction.turnRight();
        return pathToEdge.parallelStream()
                .filter(space -> canPlaceObstacleWithoutParadox(visitedPositions, direction, space))
                .filter(space -> wouldCreateLoopTurningIn(space, obstacles, space.offsetBy(direction), nextDirection))
                .map(space -> space.offsetBy(direction))
                .toList();
    }

    private boolean canPlaceObstacleWithoutParadox(final Set<Position> visitedPositions, final Position direction, final Position space) {
        return !visitedPositions.contains(space.offsetBy(direction));
    }

    private boolean wouldCreateLoopTurningIn(Position guardPosition, final Set<Position> obstacles, final Position newObstacle, final Position startingDirection) {
        final Set<Position> subObstacles = new HashSet<>(obstacles);
        subObstacles.add(newObstacle);
        final Map<Position, Set<Position>> subTurningPoints = Map.of(
                UP, new HashSet<>(),
                RIGHT, new HashSet<>(),
                DOWN, new HashSet<>(),
                LEFT, new HashSet<>()
        );
        Position direction = startingDirection;
        Optional<Position> spaceBeforeNextObstacle;
        while (true) {
            spaceBeforeNextObstacle = findSpaceBeforeNextObstacle(subObstacles, guardPosition, direction);
            if (spaceBeforeNextObstacle.isPresent()) {
                final Position spaceBeforeObstacle = spaceBeforeNextObstacle.get();
                if (subTurningPoints.get(direction).contains(spaceBeforeObstacle)) {
                    return true;
                }
                subTurningPoints.get(direction).add(spaceBeforeObstacle);
                final Position nextDirection = direction.turnRight();
                guardPosition = spaceBeforeObstacle;
                direction = nextDirection;
            } else {
                return false;
            }
        }
    }
}
