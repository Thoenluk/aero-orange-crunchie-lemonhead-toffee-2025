package ch.thoenluk.solvers.WhyDoWeGiveOurRobotsInfiniteRenewablePower;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.ut.Position;
import ch.thoenluk.ut.UtMath;
import ch.thoenluk.ut.UtParsing;
import ch.thoenluk.ut.UtStrings;

import java.util.*;

import static ch.thoenluk.ut.Position.*;

@Day(15)
public class BoxPusherPushinator implements ChristmasSaver {
    public static final char FLOOR = '.';
    public static final char ROBOT = '@';
    public static final char BOX = 'O';
    public static final char WALL = '#';
    public static final char LEFT_BOX = '['; // Why is it labelled "Shark"?
    public static final char RIGHT_BOX = ']'; // This one is also labelled "Shark"! What is it the lanternfish eat anyway?

    @Override
    public String saveChristmas(final String input) {
        return Warehouse.fromDescription(input).shuffleBoxesAround();
    }


    @Override
    public String saveChristmasAgain(final String input) {
        return Warehouse.fromDescription(input).widen().shuffleWideBoxesAround();
    }

    private record Warehouse(Map<Position, Character> map, List<Position> attemptedMovements) {
        public static Warehouse fromDescription(final String description) {
            final String[] parts = UtStrings.splitStringWithEmptyLines(description);
            final Map<Position, Character> map = UtParsing.multilineStringToPositionCharacterMap(parts[0]);
            final List<Position> attemptedMovements = parts[1].replaceAll(UtStrings.WHITE_SPACE_REGEX, "").chars()
                    .mapToObj(Warehouse::toDirection)
                    .toList();
            return new Warehouse(map, attemptedMovements);
        }

        private static Position toDirection(final int description) {
            return switch (description) {
                case '^' -> UP;
                case '>' -> RIGHT;
                case 'v' -> Position.DOWN;
                case '<' -> Position.LEFT;
                default -> throw new AssertionError(STR."Couldn't map direction \{description} for some reason!");
            };
        }

        public Warehouse widen() {
            final Map<Position, Character> newMap = new HashMap<>(map.size());
            map.forEach((originalPosition, value) -> {
                final char leftTile = value == BOX ? LEFT_BOX : value;
                final char rightTile = switch (value) {
                    case BOX -> RIGHT_BOX;
                    case ROBOT -> FLOOR;
                    default -> value;
                };
                final Position leftPosition = new Position(originalPosition.y(), originalPosition.x() * 2); // Hilbert called about a reservation?
                final Position rightPosition = new Position(originalPosition.y(), originalPosition.x() * 2 + 1);
                newMap.put(leftPosition, leftTile);
                newMap.put(rightPosition, rightTile);
            });
            return new Warehouse(newMap, attemptedMovements);
        }

        private String shuffleBoxesAround() {
            Position robotLocation = locateRobot();
            for (final Position movement : attemptedMovements) {
                robotLocation = executeMovement(robotLocation, movement);
            }
            return determineGPSScore();
        }

        private String shuffleWideBoxesAround() {
            Position robotLocation = locateRobot();
            for (final Position movement : attemptedMovements) {
                robotLocation = executeWideMovement(robotLocation, movement);
            }
            return determineGPSScore();
        }

        private Position locateRobot() {
            final Position robotLocation = map.entrySet().stream()
                    .filter(entry -> entry.getValue() == ROBOT)
                    .map(Map.Entry::getKey)
                    .findAny()
                    .orElseThrow();
            map.put(robotLocation, FLOOR); // The robot does not exist. The robot is a government spy bird.
            return robotLocation;
        }

        private Position executeMovement(final Position robotLocation, final Position movement) {
            final Position nextLocation = robotLocation.offsetBy(movement);
            final Character terrain = map.get(nextLocation);
            if (terrain == FLOOR) {
                return nextLocation;
            }
            if (terrain == WALL) {
                return robotLocation;
            }
            Position boxPushLocation = nextLocation.offsetBy(movement);
            while (map.get(boxPushLocation) == BOX) {
                boxPushLocation = boxPushLocation.offsetBy(movement);
            }
            if (map.get(boxPushLocation) == WALL) {
                return robotLocation;
            }
            map.put(boxPushLocation, BOX);
            map.put(nextLocation, FLOOR);
            return nextLocation;
        }

        private String determineGPSScore() {
            return map.entrySet().stream()
                    .filter(entry -> entry.getValue() == BOX || entry.getValue() == LEFT_BOX)
                    .map(Map.Entry::getKey)
                    .map(tile -> tile.y() * 100 + tile.x())
                    .reduce(UtMath::overflowSafeSum)
                    .map(Object::toString)
                    .orElseThrow();
        }

        private Position executeWideMovement(final Position robotLocation, final Position movement) {
            final List<Position> tilesToExamine = new LinkedList<>();
            tilesToExamine.add(robotLocation);
            final Set<Position> examinedTiles = new HashSet<>();
            final Map<Position, Character> changes = new HashMap<>();
            while (!tilesToExamine.isEmpty()) {
                final Position tileToExamine = tilesToExamine.removeFirst();
                final Position nextTile = tileToExamine.offsetBy(movement);
                final char nextTerrain = map.get(nextTile);
                if (nextTerrain == WALL) {
                    return robotLocation;
                }
                examinedTiles.add(tileToExamine);
                changes.put(nextTile, map.get(tileToExamine));
                if (nextTerrain == LEFT_BOX) {
                    tilesToExamine.add(nextTile);
                    if (isVertical(movement)) {
                        tilesToExamine.add(nextTile.offsetBy(RIGHT));
                    }
                } else if (nextTerrain == RIGHT_BOX) {
                    tilesToExamine.add(nextTile);
                    if (isVertical(movement)) {
                        tilesToExamine.add(nextTile.offsetBy(LEFT));
                    }
                }
            }
            examinedTiles.forEach(tile -> changes.putIfAbsent(tile, FLOOR));
            map.putAll(changes);
            return robotLocation.offsetBy(movement);
        }

        private boolean isVertical(final Position movement) {
            return UP == movement || DOWN == movement;
        }
    }
}
