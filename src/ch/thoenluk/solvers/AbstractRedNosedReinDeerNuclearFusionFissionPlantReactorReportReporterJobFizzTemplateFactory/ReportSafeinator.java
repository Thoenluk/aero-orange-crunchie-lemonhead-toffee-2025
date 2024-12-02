package ch.thoenluk.solvers.AbstractRedNosedReinDeerNuclearFusionFissionPlantReactorReportReporterJobFizzTemplateFactory;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.ut.UtParsing;
import ch.thoenluk.ut.UtStrings;

import java.util.*;
import java.util.stream.IntStream;

@Day(2)
public class ReportSafeinator implements ChristmasSaver {
    @Override
    public String saveChristmas(final String input) {
        return Long.toString(UtStrings.streamInputAsLines(input)
                .filter(this::reportIsSafe)
                .count());
    }

    @Override
    public String saveChristmasAgain(final String input) {
        return Long.toString(UtStrings.streamInputAsLines(input)
                .filter(this::reportIsSafeEnoughForActualBudget)
                .count());
    }

    private boolean reportIsSafe(final String report) {
        return reportIsSafe(parseLevels(report));
    }

    private static List<Integer> parseLevels(final String report) {
        return Arrays.stream(report.split(UtStrings.WHITE_SPACE_REGEX))
                .map(UtParsing::cachedParseInt)
                .toList();
    }

    private boolean reportIsSafe(final List<Integer> levels) {
        return allTransitionsAreSafe(levels);
    }

    private boolean reportIsSafeEnoughForActualBudget(final String report) {
        final List<Integer> levels = parseLevels(report);
        return reportIsSafe(levels)
                || IntStream.range(0, levels.size())
                .anyMatch(index -> allTransitionsAreSafe(safeifyLevels(index, levels)));
        // There is a more elegant way to go about this: Identify potential problem points, then only remove them.
        // However, there are some gotchas I've been able to identify:
        // * The level to ignore may be the very first or second one, throwing off whether the report is ascending or descending
        // * A level that is problematic may only be problematic from the left or the right, or both.
        // In the end, there just isn't reason to care with as few levels as we have to deal with per report.
        // It may even cost performance, given we save on average half a report's width of checks.
        // Also: I tried to implement ignoring a specific level without copying the list, but it didn't work right away
        //   and just then I was struck by an excessively poorly timed IDE crash, so I had to spend a few minutes
        //   undoing the experiment rather than being able to undo.
        // We learn: Commit, then experiment. Kinda the point of git.
    }

    private static List<Integer> safeifyLevels(final int unsafeIndex, final List<Integer> levels) {
        final List<Integer> safeifiedLevels = new ArrayList<>(levels);
        safeifiedLevels.remove(unsafeIndex);
        return safeifiedLevels;
    }

    private static boolean allTransitionsAreSafe(final List<Integer> levels) {
        final int reportSign = levels.get(0).compareTo(levels.get(1));
        return IntStream.range(0, levels.size() - 1)
                .noneMatch(index -> levelsAreUnsafe(levels.get(index), levels.get(index + 1), reportSign));
    }

    private static boolean levelsAreUnsafe(final int firstLevel, final int secondLevel, final int reportSign) {
        final int difference = firstLevel - secondLevel;

        if (Math.round(Math.signum(difference)) != reportSign) {
            return true;
        }
        final int magnitude = Math.abs(difference);
        return magnitude == 0 || magnitude > 3;
    }
}
