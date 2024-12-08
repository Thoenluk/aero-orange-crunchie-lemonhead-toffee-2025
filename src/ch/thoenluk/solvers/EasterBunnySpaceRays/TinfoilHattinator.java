package ch.thoenluk.solvers.EasterBunnySpaceRays;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.ut.Position;
import ch.thoenluk.ut.UtCollections;
import ch.thoenluk.ut.UtParsing;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

@Day(8)
public class TinfoilHattinator implements ChristmasSaver {
    private static final Character DOT = '.';

    @Override
    public String saveChristmas(final String input) {
        final Map<Position, Character> map = UtParsing.multilineStringToPositionCharacterMap(input);
        return findUniqueAntinodes(map, this::findAntiNodes);
    }

    private String findUniqueAntinodes(final Map<Position, Character> map, final Function<List<Position>, Stream<Position>> antiNodesFinder) {
        return Long.toString(map.values().parallelStream()
                .filter(frequency -> !DOT.equals(frequency))
                .distinct()
                .map(frequency -> UtCollections.findPositionsWithValueInMap(map, frequency))
                .map(Stream::toList)
                .flatMap(UtCollections::streamPairwise)
                .flatMap(antiNodesFinder)
                .distinct()
                .filter(map::containsKey)
                .count());
    }

    private Stream<Position> findAntiNodes(final List<Position> antennae) {
        final Position first = antennae.getFirst();
        final Position second = antennae.getLast();
        return Stream.of(first.mirrorOn(second), second.mirrorOn(first));
    }

    @Override
    public String saveChristmasAgain(final String input) {
        final Map<Position, Character> map = UtParsing.multilineStringToPositionCharacterMap(input);
        final int maxCoordinate = map.keySet().stream()
                .max(Position::compareAsCoordinates)
                .map(position -> Math.max(position.y(), position.x()))
                .orElseThrow();
        return findUniqueAntinodes(map, buildFindAntiNodesWithResonantHarmonics(maxCoordinate)); // In theory, the map
        // may have arbitrary boundaries at any coordinates, it may even be a complex shape.
        // In practice, it's a square.
        // Treating what you know to be a square like a square is why you hire engineers and not mathematicians.
    }

    private Function<List<Position>, Stream<Position>> buildFindAntiNodesWithResonantHarmonics(final int maxCoordinate) {
        return antennae -> findAntiNodesWithResonantHarmonics(antennae, maxCoordinate);
    }

    private Stream<Position> findAntiNodesWithResonantHarmonics(final List<Position> antennae, final int maxCoordinate) {
        final Position first = antennae.getFirst();
        final Position second = antennae.getLast();
        return Stream.concat(
                first.mirrorOnUntilMaxCoordinate(second, maxCoordinate).stream(),
                second.mirrorOnUntilMaxCoordinate(first, maxCoordinate).stream()
        );
    }
}
