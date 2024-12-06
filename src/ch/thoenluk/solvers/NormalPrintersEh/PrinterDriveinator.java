package ch.thoenluk.solvers.NormalPrintersEh;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.ut.UtStrings;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.IntStream;

@Day(5)
public class PrinterDriveinator implements ChristmasSaver {
    public static final ScopedValue<Map<String, Set<String>>> RULES = ScopedValue.newInstance();

    @Override
    public String saveChristmas(final String input) {
        return doTheThing(input, true);
    }

    // To save Christmas again, use the same logic but instead of validating jobs, order the jobs' pages
    // in order of least to most pages from among the job that must come first.
    // For the legal first page, it must be true that the intersection between other pages in the job
    // and pages that must come before it is an empty set.
    // Following pages can have at most n pages coming before them.
    // And so I completely called that and my solution that I wrote up before even seeing the second challenge worked perfectly.
    // I am literally the best.

    @Override
    public String saveChristmasAgain(final String input) {
        return doTheThing(input, false);
    }

    private String doTheThing(final String input, final boolean jobsShouldBeCorrectlyOrdered) {
        try {
            return ScopedValue.where(RULES, new HashMap<>())
                    .call(() -> parseRulesAndFindMiddlePages(input, jobsShouldBeCorrectlyOrdered));
        } catch (final Exception e) {
            throw new AssertionError(e);
        }
    }

    private String parseRulesAndFindMiddlePages(final String input, final boolean jobsShouldBeCorrectlyOrdered) {
        final String[] parts = UtStrings.splitStringWithEmptyLines(input);
        parseRules(parts[0]);
        return Integer.toString(findMiddlePages(parts[1], jobsShouldBeCorrectlyOrdered));
    }

    private void parseRules(final String stringRepresentation) {
        UtStrings.streamInputAsLines(stringRepresentation).forEach(this::parseRule);
    }

    private void parseRule(final String stringRepresentation) {
        final String[] parts = stringRepresentation.split("\\|");
        final String precedingPage = parts[0];
        final String followingPage = parts[1];
        getRulesFor(followingPage).add(precedingPage);
    }

    private static Set<String> getRulesFor(final String followingPage) {
        return RULES.get().computeIfAbsent(followingPage, _ -> new HashSet<>());
    }

    private int findMiddlePages(final String printJobs, final boolean jobsShouldBeCorrectlyOrdered) {
        final Predicate<List<String>> sortedAsDesired = jobsShouldBeCorrectlyOrdered
                ? this::isProperlySorted
                : Predicate.not(this::isProperlySorted);
        return UtStrings.streamInputAsLines(printJobs)
                .map(UtStrings::splitCommaSeparatedString)
                .map(List::of)
                .filter(sortedAsDesired)
                .map(this::sortPrintJob) // This call is completely unnecessary on the first challenge.
                // However, it allows me to completely reuse code between challenges and my ego is mega huge right now.
                .map(this::findMiddleElement)
                .map(Integer::parseInt).reduce(0, Integer::sum);
    }

    private boolean isProperlySorted(final List<String> printJob) {
        return IntStream.range(0, printJob.size()).allMatch(index -> noRulesViolated(printJob, index));
    }

    private boolean noRulesViolated(final List<String> printJob, final int index) {
        final Set<String> pagesThatMustComeFirst = getRulesFor(printJob.get(index));
        return printJob.subList(index, printJob.size()).stream().noneMatch(pagesThatMustComeFirst::contains);
    }

    private List<String> sortPrintJob(final List<String> printJob) {
        return printJob.stream()
                .sorted(Comparator.comparing(page -> countPagesInJobThatMustComeFirst(page, printJob)))
                .toList();
    }

    private int countPagesInJobThatMustComeFirst(final String page, final List<String> printJob) {
        final Set<String> pages = new HashSet<>(printJob);
        pages.retainAll(getRulesFor(page));
        return pages.size();
    }

    private String findMiddleElement(final List<String> printJob) {
        return printJob.get(printJob.size() / 2);
    }
}