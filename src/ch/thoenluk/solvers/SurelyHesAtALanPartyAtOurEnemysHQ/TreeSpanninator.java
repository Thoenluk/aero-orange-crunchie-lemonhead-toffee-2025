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
        final Set<String> computersStartingWithT = network.computersStartingWithT();
        return Long.toString(findTripleConnections(network.connections()).stream()
                .filter(tripleConnection -> UtCollections.anyOverlap(tripleConnection, computersStartingWithT))
                .count());
    }

    private Network parseNetwork(final String input) {
        final Map<String, Set<String>> connections = new HashMap<>();
        final Set<String> computersStartingWithT = new HashSet<>();
        UtStrings.streamInputAsLines(input).forEach(description -> parseConnection(description, connections, computersStartingWithT));
        return new Network(connections, computersStartingWithT);
    }

    private void parseConnection(final String description, final Map<String, Set<String>> connections, final Set<String> computersStartingWithT) {
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
        connections.computeIfAbsent(lesser, _ -> new HashSet<>());
        connections.get(lesser).add(greater);
        if (sides[0].startsWith("t")) {
            computersStartingWithT.add(left);
        }
        if (sides[1].startsWith("t")) {
            computersStartingWithT.add(right);
        }
    }

    private Set<Set<String>> findTripleConnections(final Map<String, Set<String>> connections) {
        final Set<Set<String>> result = new HashSet<>();
        for (final String first : connections.keySet()) {
            final Set<String> connectedComputers = connections.get(first);
            for (final String second : connectedComputers) {
                final Set<String> thirds = findOverlap(connections.get(second), connectedComputers);
                thirds.forEach(third -> result.add(Set.of(first, second, third)));
            }
        }
        return result;
    }

    private Set<String> findOverlap(final Set<String> first, final Set<String> second) {
        if (first == null) {
            return Set.of();
        }
        final Set<String> overlap = new HashSet<>(first);
        overlap.retainAll(second);
        return overlap;
    }

    @Override
    public String saveChristmasAgain(final String input) {
        final Map<String, Set<String>> connections = parseNetwork(input).connections();
        return connections.keySet().stream()
                .map(computer -> spanTheTree(computer, connections))
                .flatMap(Collection::stream)
                .max(Comparator.comparing(Collection::size))
                .map(this::toCodeKata)
                .orElseThrow();
    }

    private List<Set<String>> spanTheTree(final String computer, final Map<String, Set<String>> connections) {
        return spanTheTree(computer, connections.get(computer), Set.of(computer), connections);
    }

    // Decking the halls was deprecated in 2022
    private List<Set<String>> spanTheTree(final String computer, final Set<String> allowedConnections, final Set<String> computersToConnectTo, final Map<String, Set<String>> connections) {
        final Set<String> connectionsToInvestigate = findOverlap(connections.get(computer), allowedConnections);
        final Set<String> network = new HashSet<>(computersToConnectTo);
        network.add(computer);
        if (connectionsToInvestigate.isEmpty()) {
            return List.of(network);
        }
        final List<Set<String>> result = new LinkedList<>();
        for (final String connection : connectionsToInvestigate) {
            result.addAll(spanTheTree(connection, connectionsToInvestigate, network, connections));
        }
        return result;
    }

    private String toCodeKata(final Set<String> network) {
        final StringBuilder builder = new StringBuilder();
        network.stream()
                .sorted(Comparator.naturalOrder())
                .forEach(computer -> builder.append(computer).append(','));
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    private record Network(Map<String, Set<String>> connections, Set<String> computersStartingWithT) {}
}
