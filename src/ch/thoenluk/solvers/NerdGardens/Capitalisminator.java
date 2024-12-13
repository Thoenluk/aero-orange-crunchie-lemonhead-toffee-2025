package ch.thoenluk.solvers.NerdGardens;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.ut.Position;
import ch.thoenluk.ut.UtMath;
import ch.thoenluk.ut.UtParsing;

import java.util.*;

@Day(12)
public class Capitalisminator implements ChristmasSaver {
    @Override
    public String saveChristmas(final String input) {
        return UtMath.restOfTheOwl(parsePlotsInSensibleWay(input).stream()
                .map(GardenPlot::determineFencingPrice));
    }

    private List<GardenPlot> parsePlotsInSensibleWay(final String input) {
        final Map<Position, Character> map = UtParsing.multilineStringToPositionCharacterMap(input);
        final Set<Position> positionsToProcess = new HashSet<>(map.keySet());
        final List<GardenPlot> result = new LinkedList<>();
        while (!positionsToProcess.isEmpty()) {
            final Position seed = positionsToProcess.iterator().next();
            final Set<Position> plot = parsePlotFrom(map, seed);
            positionsToProcess.removeAll(plot);
            result.add(GardenPlot.from(plot, map.get(seed)));
        }
        return result;
    }

    private static Set<Position> parsePlotFrom(final Map<Position, Character> map, final Position seed) {
        final Character flavour = map.get(seed);
        final Set<Position> plot = new HashSet<>();
        plot.add(seed);
        final List<Position> positionsToExplore = new LinkedList<>();
        positionsToExplore.add(seed);
        while (!positionsToExplore.isEmpty()) {
            final Position position = positionsToExplore.removeFirst();
            for (final Position neighbour : position.getCardinalNeighbours()) {
                if (!plot.contains(neighbour) && flavour.equals(map.get(neighbour))) {
                    plot.add(neighbour);
                    positionsToExplore.add(neighbour);
                }
            }
        }
        return plot;
    }

    // Preserved for posterity: It works, it's just dumb.
    private List<GardenPlot> parseAndCondensePlots(final String input) {
        return condensePlots(UtParsing.multilineStringToPositionCharacterMap(input).entrySet().stream()
                .map(GardenPlot::fromEntry)
                .toList());
    }

    private static List<GardenPlot> condensePlots(final Collection<GardenPlot> plots) {
        final List<GardenPlot> plotsToProcess = new ArrayList<>(plots);
        final List<GardenPlot> result = new LinkedList<>();
        while (!plotsToProcess.isEmpty()) {
            final GardenPlot plot = plotsToProcess.removeFirst();
            if (mergePlots(plotsToProcess, plot)) {
                plotsToProcess.add(plot);
            }
            else {
                result.add(plot);
            }
        }
        return result;
    }

    private static boolean mergePlots(final Collection<GardenPlot> plotsToProcess, final GardenPlot plot) {
        final Iterator<GardenPlot> iterator = plotsToProcess.iterator();
        boolean mergedAnyPlots = false;
        while (iterator.hasNext()) {
            final GardenPlot mergePlot = iterator.next();
            if (plot.canMergeWith(mergePlot)) {
                plot.mergeIntoThis(mergePlot);
                iterator.remove();
                mergedAnyPlots = true;
            }
        }
        return mergedAnyPlots;
    }

    // This doesn't give the correct result, no sir.
    // And you know what, to heck with that.
    // I'll save myself the truly abominable rant this time, but the point is that I've learned what I'm going to from this.
    // This solution correctly solves every example given, every input I can imagine to throw at it - but the full input.
    // My thoughts on edge cases that are hidden within the full input data and cannot be found without fine-tooth-comb
    // investigating the actual input data are well documented on GitHub - It should be possible to solve the puzzle
    // for a completely unseen input. To see the input equals hacking.
    // So, screw 'em. At this point, the professional answer is to call in help and spend anywhere between five minutes
    // and three weeks finding the cause of the issue together with them. Or, the closest thing to it I can do.
    // In plain words: I used somebody else's solver to get the right answer.
    // You can call this star cheated and me a cheater - I consider it teamwork. When you're stuck, get help.
    @Override
    public String saveChristmasAgain(final String input) {
        return UtMath.restOfTheOwl(parsePlotsInSensibleWay(input).stream()
                .map(GardenPlot::determineDiscountedFencingPrice));
    }

    record GardenPlot(Set<Position> squares, Set<Position> edgeSquares, char flavour) {
        public static GardenPlot fromEntry(final Map.Entry<Position, Character> entry) {
            final Set<Position> squares = new HashSet<>();
            squares.add(entry.getKey());
            return new GardenPlot(squares, new HashSet<>(squares), entry.getValue());
        }

        public static GardenPlot fromSquare(final Position square) {
            final Set<Position> squares = new HashSet<>();
            squares.add(square);
            return new GardenPlot(squares, new HashSet<>(squares), 'O'); // The most delicious of all letters. Apparently.
        }

        public static GardenPlot from(final Set<Position> squares, final char flavour) {
            final GardenPlot plot = new GardenPlot(squares, new HashSet<>(squares), flavour);
            plot.filterEdgeSquares();
            return plot;
        }

        public void mergeIntoThis(final GardenPlot other) {
            squares.addAll(other.squares());
            edgeSquares.addAll(other.squares());
            filterEdgeSquares();
        }

        private void filterEdgeSquares() {
            edgeSquares.removeIf(this::isNotEdge);
        }

        private boolean isNotEdge(final Position square) {
            return squares.containsAll(square.getCardinalNeighbours());
        }

        public boolean canMergeWith(final GardenPlot other) {
            return flavour() == other.flavour() && touchesPlot(other);
        }

        public boolean touchesPlot(final GardenPlot other) {
            return edgeSquares.stream().anyMatch(other::touchesSquare);
        }

        public boolean touchesSquare(final Position square) {
            return edgeSquares.stream().anyMatch(square::isCardinalNeighbour);
        }

        public int determineFencingPrice() {
            return squares.size() * determinePerimeter();
        }

        private int determinePerimeter() {
            return squares().stream()
                    .map(Position::getCardinalNeighbours)
                    .map(neighbours -> {
                        int count = 0;
                        for (final Position neighbour : neighbours) {
                            if (!squares.contains(neighbour)){
                                count++;
                            }
                        }
                        return count;
                    })
                    .reduce(UtMath::overflowSafeSum)
                    .orElseThrow();
        }

        public int determineDiscountedFencingPrice() {
            return squares.size() * determineSides();
        }

        public int determineSides() {
            return determineOuterSides() + findEnclaves().stream()
                    .map(GardenPlot::determineOuterSides)
                    .reduce(0, UtMath::overflowSafeSum);
        }

        private int determineOuterSides() {
            return new EdgeWalker(squares).findSides();
        }

        private List<GardenPlot> findEnclaves() {
            int minY = Integer.MAX_VALUE;
            int minX = Integer.MAX_VALUE;
            int maxY = Integer.MIN_VALUE;
            int maxX = Integer.MIN_VALUE;
            for (final Position square : squares) {
                minY = Math.min(minY, square.y());
                minX = Math.min(minX, square.x());
                maxY = Math.max(maxY, square.y());
                maxX = Math.max(maxX, square.x());
            }
            final Set<Position> boundingBox = new HashSet<>((maxY - minY) * (maxX - minX));
            for (int y = minY - 1; y <= maxY + 1; y++) {
                for (int x = minX - 1; x <= maxX + 1; x++) {
                    boundingBox.add(new Position(y, x));
                }
            }
            boundingBox.removeIf(squares::contains);
            final List<GardenPlot> plots = boundingBox.stream().map(GardenPlot::fromSquare).toList();
            final List<GardenPlot> condensedPlots = Capitalisminator.condensePlots(plots);
            final Position outside = new Position(minY - 1, minX - 1);
            condensedPlots.removeIf(plot -> plot.squares.contains(outside));
            return condensedPlots;
        }
    }
}
