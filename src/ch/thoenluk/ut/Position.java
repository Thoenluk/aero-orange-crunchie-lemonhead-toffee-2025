package ch.thoenluk.ut;

import java.util.LinkedList;
import java.util.List;

public record Position(int y, int x) {

    public static final Position UP = new Position(-1, 0);
    public static final Position DOWN = new Position(1, 0);
    public static final Position LEFT = new Position(0, -1);
    public static final Position RIGHT = new Position(0, 1);
    public static final Position UP_LEFT = new Position(-1, -1);
    public static final Position UP_RIGHT = new Position(-1, 1);
    public static final Position DOWN_LEFT = new Position(1, -1);
    public static final Position DOWN_RIGHT = new Position(1, 1);
    public static final Position SELF = new Position(0, 0);


    //---- Static Methods

    public static Position fromString(final String description) {
        final String[] coordinates = UtStrings.splitCommaSeparatedString(description);
        return new Position(UtParsing.cachedParseInt(coordinates[1]), UtParsing.cachedParseInt(coordinates[0]));
    }


    //---- Methods

    public int getDistanceFrom(final Position other) {
        return Math.abs(x() - other.x())
                + Math.abs(y() - other.y());
    }

    public int getRoleplayingDistanceFrom(final Position other) {
        return Math.max(
                Math.abs(x() - other.x()),
                Math.abs(y() - other.y())
        );
    }

    public boolean isInDirectionOf(final Position other, final Position direction) {
        return isInDirectionOfSingleCoordinate(y(), other.y(), direction.y())
                && isInDirectionOfSingleCoordinate(x(), other.x(), direction.x());
    }

    private boolean isInDirectionOfSingleCoordinate(final int coordinateOfThis, final int coordinateOfOther, final int coordinateOfDirection) {
        if (coordinateOfDirection == 0) {
            return coordinateOfThis == coordinateOfOther;
        }
        final int distance = coordinateOfThis - coordinateOfOther;
        return Math.round(Math.signum(distance)) == Math.round(Math.signum(coordinateOfDirection))
            && distance % coordinateOfDirection == 0;
    }

    public List<Position> getNeighbours(final NeighbourDirection neighbourDirection) {
        final List<Position> neighbours = new LinkedList<>();
        for (final Position direction : neighbourDirection.getDirections()) {
            neighbours.add(offsetBy(direction));
        }
        return neighbours;
    }

    public List<Position> getCardinalNeighbours() {
        return getNeighbours(NeighbourDirection.CARDINAL);
    }

    public List<Position> getDiagonalNeighbours() {
        return getNeighbours(NeighbourDirection.DIAGONAL);
    }

    public List<Position> getOmnidirectionalNeighbours() {
        return getNeighbours(NeighbourDirection.OMNIDIRECTIONAL);
    }

    public Position invert() {
        return multiply(-1);
    }

    public Position multiply(final int multiplier) {
        return new Position(y() * multiplier, x() * multiplier);
    }

    public Position offsetBy(final int y, final int x) {
        return offsetBy(new Position(y, x));
    }

    public Position offsetBy(final Position offset) {
        return new Position(this.y + offset.y(), this.x + offset.x());
    }

    public Position offsetByDistance(final Position offset, final int distance) {
        return new Position(this.y() + offset.y() * distance, this.x() + offset.x() * distance);
    }

    public int compareAsCoordinates(final Position other) {
        if (y() != other.y()) {
            return y() - other.y();
        }
        return x() - other.x();
    }

    public List<Position> offsetBy(final List<Position> offsets) {
        final List<Position> offsetPositions = new LinkedList<>();
        for (final Position offset : offsets) {
            offsetPositions.add(offsetBy(offset));
        }
        return offsetPositions;
    }

    public List<Position> getPathTo(final Position other) {
        final List<Position> path = new LinkedList<>();
        final Position offset = new Position(other.y() - y(), other.x() - x());

        if (
                !(
                    offset.y() == 0
                    || offset.x() == 0
                    || Math.abs(offset.y()) == Math.abs(offset.x())
                )
        ) {
            throw new IllegalArgumentException(String.format("Requested path between %s and %s would require sub-Position precision!", this, other));
        }

        final Position direction = new Position(
                (int) Math.signum(offset.y()),
                (int) Math.signum(offset.x())
        );

        for (int i = 0; i <= getRoleplayingDistanceFrom(other); i++) {
            path.add(this.offsetBy(new Position(direction.y() * i, direction.x() * i)));
        }

        return path;
    }


    //---- Inner enum

    public enum NeighbourDirection {
        CARDINAL(List.of(UP, LEFT, RIGHT, DOWN)),
        CARDINAL_AND_SELF(List.of(UP, LEFT, RIGHT, DOWN, SELF)),
        DIAGONAL(List.of(UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT)),
        OMNIDIRECTIONAL(List.of(UP_LEFT, UP, UP_RIGHT,
                                LEFT,           RIGHT,
                                DOWN_LEFT, DOWN, DOWN_RIGHT
        )),
        OMNIDIRECTIONAL_AND_SELF(List.of(UP_LEFT, UP, UP_RIGHT,
                                        LEFT, SELF, RIGHT,
                                        DOWN_LEFT, DOWN, DOWN_RIGHT
        ));

        private final List<Position> directions;

        /* private */ NeighbourDirection(final List<Position> directions) {
            this.directions = directions;
        }

        public List<Position> getDirections() {
            return directions;
        }
    }
}