package ch.thoenluk.solvers.IfTheyCanTeleportWhyDidIDoAllThatStuff;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.ut.Position;
import ch.thoenluk.ut.UtCollections;
import ch.thoenluk.ut.UtMath;
import ch.thoenluk.ut.UtParsing;

import java.util.List;
import java.util.Map;

@Day(4)
public class MeaningOfXmasLocatinator implements ChristmasSaver {
    private static final Character X = 'X';
    private static final Character M = 'M';
    private static final Character A = 'A';
    private static final Character S = 'S';
    private static final List<Position> DIAGONAL_CROSS_NEIGHBOURS = List.of(Position.UP_LEFT, Position.UP_RIGHT);

    @Override
    public String saveChristmas(final String input) {
        final Map<Position, Character> grid = UtParsing.multilineStringToPositionCharacterMap(input);
        return findXmases(grid);
    }

    private String findXmases(final Map<Position, Character> grid) {
        return UtMath.restOfTheLongOwl(UtCollections.findPositionsWithValueInMap(grid, X) //
                .map(position -> findNumberOfMasFrom(position, grid)));
    }

    private long findNumberOfMasFrom(final Position start, final Map<Position, Character> grid) {
        return Position.NeighbourDirection.OMNIDIRECTIONAL.getDirections().stream() //
                .filter(direction -> gridHasMasInDirection(start, direction, grid)) //
                .count();
    }

    private boolean gridHasMasInDirection(final Position start, final Position direction, final Map<Position, Character> grid) {
        final Position shouldBeM = start.offsetBy(direction);
        if (!M.equals(grid.get(shouldBeM))) {
            return false;
        }
        final Position shouldBeA = shouldBeM.offsetBy(direction);
        return A.equals(grid.get(shouldBeA)) && S.equals(grid.get(shouldBeA.offsetBy(direction)));
    }

    // Fun fact: If you count cardinal MASes too with my input, you get 1987. Scott Cawthon has done it again.
    // So this, which wasn't made clear shouldn't count because we have been trained to assume things not explicitly stated do count:
    //   M
    //  SAM
    //   S
    @Override
    public String saveChristmasAgain(final String input) {
        final Map<Position, Character> grid = UtParsing.multilineStringToPositionCharacterMap(input);
        return findCrossedMases(grid);
    }

    private String findCrossedMases(final Map<Position, Character> grid) {
        return Long.toString(UtCollections.findPositionsWithValueInMap(grid, A)
                .filter(position -> isCenterOfCrossedMas(grid, position))
                .count());
    }

    private boolean isCenterOfCrossedMas(final Map<Position, Character> grid, final Position position) {
        return MeaningOfXmasLocatinator.DIAGONAL_CROSS_NEIGHBOURS.stream()
                .allMatch(direction -> isMas(grid, position, direction));
    }

    private boolean isMas(final Map<Position, Character> grid, final Position position, final Position direction) {
        final Position neighbour = position.offsetBy(direction);
        final Position oppositeNeighbour = position.offsetBy(direction.invert());
        return (M.equals(grid.get(neighbour)) && S.equals(grid.get(oppositeNeighbour)))
                || (S.equals(grid.get(neighbour)) && M.equals(grid.get(oppositeNeighbour)));
    }
}
