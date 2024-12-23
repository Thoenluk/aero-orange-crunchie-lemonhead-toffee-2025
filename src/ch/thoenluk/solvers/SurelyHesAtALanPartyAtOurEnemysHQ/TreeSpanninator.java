package ch.thoenluk.solvers.SurelyHesAtALanPartyAtOurEnemysHQ;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.ut.UtCollections;
import ch.thoenluk.ut.UtStrings;

import java.util.*;

@Day(23)
public class TreeSpanninator implements ChristmasSaver {
    @Override
    public String saveChristmas(final String input) {
        final Network network = parseNetwork(input);
        final List<String> computersStartingWithT = network.computersStartingWithT();
        return Long.toString(findTripleConnections(network.connections()).parallelStream()
                .filter(tripleConnection -> UtCollections.anyOverlap(tripleConnection, computersStartingWithT))
                .count());
    }

    private Network parseNetwork(final String input) {
        final Map<String, List<String>> connections = new HashMap<>();
        final List<String> computersStartingWithT = new LinkedList<>();
        UtStrings.streamInputAsLines(input).forEach(description -> parseConnection(description, connections, computersStartingWithT));
        return new Network(connections, computersStartingWithT);
    }

    private void parseConnection(final String description, final Map<String, List<String>> connections, final List<String> computersStartingWithT) {
        final String[] sides = description.split("-");
        final String left = sides[0];
        final String right = sides[1];
        final String lesser, greater;
        if (left.compareTo(right) <= 0) {
            lesser = left;
            greater = right;
        }
        else {
            lesser = right;
            greater = left;
        }
        connections.computeIfAbsent(lesser, _ -> new LinkedList<>());
        connections.get(lesser).add(greater);
        if (sides[0].startsWith("t")) {
            computersStartingWithT.add(left);
        }
        if (sides[1].startsWith("t")) {
            computersStartingWithT.add(right);
        }
    }

    private List<Set<String>> findTripleConnections(final Map<String, List<String>> connections) {
        final List<Set<String>> result = new LinkedList<>();
        for (final String first : connections.keySet()) {
            final List<String> connectedComputers = connections.get(first);
            for (final String second : connectedComputers) {
                final List<String> thirds = findOverlap(connections.get(second), connectedComputers);
                thirds.forEach(third -> result.add(Set.of(first, second, third)));
            }
        }
        return result;
    }

    private List<String> findOverlap(final List<String> first, final List<String> second) {
        if (first == null) {
            return List.of();
        }
        final List<String> overlap = new LinkedList<>(first);
        overlap.retainAll(second);
        return overlap;
    }

    @Override
    public String saveChristmasAgain(final String input) {
        final Map<String, List<String>> connections = parseNetwork(input).connections();
        return connections.keySet().stream()
                .map(computer -> spanTheTree(computer, connections))
                .flatMap(Collection::stream)
                .max(Comparator.comparing(Collection::size))
                .map(this::toCodeKata)
                .orElseThrow();
    }

    private List<List<String>> spanTheTree(final String computer, final Map<String, List<String>> connections) {
        return spanTheTree(computer, connections.get(computer), List.of(), connections);
    }

    // Decking the halls was deprecated in 2022
    private List<List<String>> spanTheTree(final String computer, final List<String> allowedConnections, final List<String> computersToConnectTo, final Map<String, List<String>> connections) {
        final List<String> connectionsToInvestigate = findOverlap(connections.get(computer), allowedConnections);
        final List<String> network = new LinkedList<>(computersToConnectTo);
        network.add(computer);
        if (connectionsToInvestigate.isEmpty()) {
            return List.of(network);
        }
        final List<List<String>> result = new LinkedList<>();
        for (final String connection : connectionsToInvestigate) {
            result.addAll(spanTheTree(connection, connectionsToInvestigate, network, connections));
        }
        return result;
    }

    private String toCodeKata(final List<String> network) {
        final StringBuilder builder = new StringBuilder();
        network.stream()
                .sorted(Comparator.naturalOrder())
                .forEach(computer -> builder.append(computer).append(','));
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    private record Network(Map<String, List<String>> connections, List<String> computersStartingWithT) {}
}
