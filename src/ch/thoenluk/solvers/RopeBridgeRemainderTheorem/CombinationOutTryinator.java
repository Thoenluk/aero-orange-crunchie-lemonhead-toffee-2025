package ch.thoenluk.solvers.RopeBridgeRemainderTheorem;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.ut.UtMath;
import ch.thoenluk.ut.UtParsing;
import ch.thoenluk.ut.UtStrings;

import java.util.List;

@Day(7)
public class CombinationOutTryinator implements ChristmasSaver {
    @Override
    public String saveChristmas(final String input) {
        return doTheThing(input, false);
    }

    private String doTheThing(final String input, final boolean allowConcatenation) {
        return UtMath.restOfTheLongOwl(UtStrings.streamInputAsLines(input).parallel()
                .map(line -> getCalibrationResultOrZeroIfCouldNotBeProduced(line, allowConcatenation)));
    }

    private long getCalibrationResultOrZeroIfCouldNotBeProduced(final String line, final boolean allowConcatenation) {
        final String[] parts = line.split(": ");
        final long calibrationValue = UtParsing.cachedParseLong(parts[0]);
        final List<Long> equation = UtParsing.whitespaceSeparatedStringToLongList(parts[1]);
        if (couldBeProduced(calibrationValue, equation.subList(1, equation.size()), equation.getFirst(), allowConcatenation)) {
            return calibrationValue;
        }
        return 0;
    }

    private boolean couldBeProduced(final long calibrationValue, final List<Long> equation, final long result, final boolean allowConcatenation) {
        if (result > calibrationValue) {
            return false;
        }
        if (equation.isEmpty()) {
            return calibrationValue == result;
        }
        final long nextNumber = equation.getFirst();
        final List<Long> remainingEquation = equation.subList(1, equation.size());
        return couldBeProduced(calibrationValue, remainingEquation, result + nextNumber, allowConcatenation)
                || couldBeProduced(calibrationValue, remainingEquation, result * nextNumber, allowConcatenation)
                || (allowConcatenation && couldBeProduced(calibrationValue, remainingEquation, concatenate(result, nextNumber), true));
    }

    final long concatenate(final long first, final long second) {
        final long orderOfMagnitude =  UtMath.orderOfMagnitude(second) + 1;
        return (first * UtMath.power(10, orderOfMagnitude)) + second;
    }

    @Override
    public String saveChristmasAgain(final String input) {
        return doTheThing(input, true);
    }
}
