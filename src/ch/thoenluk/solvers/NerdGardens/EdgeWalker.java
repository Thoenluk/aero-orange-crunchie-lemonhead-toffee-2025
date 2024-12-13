package ch.thoenluk.solvers.NerdGardens;

import ch.thoenluk.ut.Position;

import java.util.Collection;

import static ch.thoenluk.ut.Position.UP;

public class EdgeWalker {
    private final Collection<Position> squares;
    private final Position start;
    private Position location;
    private Position direction;
    private int sides;

    EdgeWalker(final Collection<Position> squares) {
        this.squares = squares;
        this.start = squares.stream().min(Position::compareAsCoordinates).orElseThrow();
        this.location = start;
        this.direction = Position.UP;
        this.sides = 0;
    }

    public int findSides() {
        if (sides == 0) {
            do {
                takeStep();
            } while (!(location.equals(start) && direction == UP));
        }
        return sides;
    }

    private void takeStep() {
        final Position leftward = location.offsetBy(direction.turnLeft());
        if (squares.contains(leftward)) {
            location = leftward;
            direction = direction.turnLeft();
            sides++;
            return;
        }
        final Position forward = location.offsetBy(direction);
        if (squares.contains(forward)) {
            location = forward;
            return;
        }
        final Position rightward = location.offsetBy(direction.turnRight());
        if (squares.contains(rightward)) {
            location = rightward;
        }
        direction = direction.turnRight();
        sides++;
    }
}
