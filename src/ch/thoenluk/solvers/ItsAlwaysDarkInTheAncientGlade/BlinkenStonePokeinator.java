package ch.thoenluk.solvers.ItsAlwaysDarkInTheAncientGlade;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.ut.UtMath;
import ch.thoenluk.ut.UtParsing;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Day(11)
public class BlinkenStonePokeinator implements ChristmasSaver {
    private static final Map<PokenStone, Long> CACHE = new HashMap<>();

    @Override
    public String saveChristmas(final String input) {
        return doTheThing(input, PokenStone::forInitialNumber);
    }

    private static String doTheThing(final String input, final Function<Long, PokenStone> mapper) {
        return UtMath.restOfTheLongOwl(UtParsing.whitespaceSeparatedStringToLongList(input).stream()
                .map(mapper)
                .map(PokenStone::blinkAt));
    }

    @Override
    public String saveChristmasAgain(final String input) {
        return doTheThing(input, PokenStone::forInitialNumberButBig);
    }

    private record PokenStone(long stoneNumber, int blinksRemaining) {
        public static final int INITIAL_BLINKS = 25;
        public static final int INITIAL_BLINKS_BUT_BIG = 75;
        public static final int MULTIPLIER = 2024;

        public static PokenStone forInitialNumber(final long initialStoneNumber) {
            return new PokenStone(initialStoneNumber, INITIAL_BLINKS);
        }

        public static PokenStone forInitialNumberButBig(final long initialStoneNumber) {
            return new PokenStone(initialStoneNumber, INITIAL_BLINKS_BUT_BIG);
        }

        public long blinkAt() {
            if (blinksRemaining == 0) {
                return 1;
            }
            if (!CACHE.containsKey(this)) {
                CACHE.put(this, findResultingStones());
            }
            return CACHE.get(this);
        }

        private long findResultingStones() {
            if (stoneNumber == 0) {
                return new PokenStone(1, blinksRemaining - 1).blinkAt();
            }
            final int orderOfMagnitude = UtMath.orderOfMagnitude(stoneNumber);
            if (orderOfMagnitude % 2 == 1) {
                final long boundary = UtMath.power(10, 1 + orderOfMagnitude / 2);
                return new PokenStone(stoneNumber / boundary, blinksRemaining - 1).blinkAt()
                        + new PokenStone(stoneNumber % boundary, blinksRemaining - 1).blinkAt();
            }
            return new PokenStone(UtMath.superOverflowSafeProduct(stoneNumber * MULTIPLIER), blinksRemaining - 1).blinkAt();
        }
    }
}
