package ch.thoenluk;

/**
 *
 * @author Lukas Thöni lukas.thoeni@gmx.ch
 */
public interface ChristmasSaver {

    /**
     * Save Christmas for the given input and return the reached solution.
     *
     * @param input The input as received from the AOC website.
     * @return The output as can be reported back to AOC to receive a star.
     */
    default String saveChristmas(final String input) {
        return "Idiot forgot to implement";
    }

    /**
     * Save Christmas but for the second challenge.
     *
     * @param input The input as received from the AOC website.
     * @return The output as can be reported back to AOC to receive a star.
     */
    default String saveChristmasAgain(final String input) {
        return "Idiot forgot to implement, or it's the 25th";
    }
}
