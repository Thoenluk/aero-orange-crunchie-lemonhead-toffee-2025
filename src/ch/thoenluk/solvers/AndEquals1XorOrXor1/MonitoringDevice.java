package ch.thoenluk.solvers.AndEquals1XorOrXor1;

import ch.thoenluk.ut.UtStrings;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.FormatProcessor.FMT;

class MonitoringDevice {
    private final Map<String, Boolean> registers = new HashMap<>();
    private final Map<String, List<String>> dependencies = new HashMap<>();
    private final Map<String, Gate> gates = new HashMap<>();
    private final Map<Integer, String> directSums = new HashMap<>();
    private final Map<Integer, String> directCarries = new HashMap<>();
    private final Map<Integer, String> carries = new HashMap<>();
    private final Map<Integer, String> previousCarries = new HashMap<>(); // Different from carry n-1!
    // carries is OR(previousCarry n, directCarry n-1)
    // previousCarries is AND(directCarry n-2, directSum n-1)
    private final List<String> faults = new LinkedList<>();

    MonitoringDevice(final String description) {
        final String[] parts = UtStrings.splitStringWithEmptyLines(description);
        UtStrings.streamInputAsLines(parts[1]).forEach(this::setupGate);
        UtStrings.streamInputAsLines(parts[0]).forEach(this::setupValue);
    }

    public String simulate() {
        final List<Gate> gatesToExplore = gates.values().stream()
                .filter(this::canCompute)
                .collect(Collectors.toCollection(LinkedList::new));
        while (!gatesToExplore.isEmpty()) {
            final List<Gate> computableGates = new LinkedList<>(); // Almost certainly faster if List
            for (final Gate gate : gatesToExplore) {
                final List<Boolean> inputValues = gate.getInputs().stream().map(registers::get).toList();
                registers.put(gate.getLabel(), gate.compute(inputValues));
                dependencies.getOrDefault(gate.getLabel(), List.of()).stream()
                        .filter(Predicate.not(registers::containsKey))
                        .map(gates::get)
                        .filter(this::canCompute)
                        .forEach(computableGates::add);
            }
            gatesToExplore.clear();
            gatesToExplore.addAll(computableGates);
        }
        return Long.toString(combineOutput());
    }

    public String getSchematic() {
        final StringBuilder builder = new StringBuilder();
        getOutputs().forEach(label -> builder.append(STR."\{stringifyGate(label, true)} -> \{label}\n"));
        return builder.toString();
    }

    private String stringifyGate(final String label, final boolean useParentheses) {
        if (label.startsWith("x") || label.startsWith("y")) {
            return label;
        }
        final Gate gate = gates.get(label);
        final List<String> inputs = gate.getInputs().stream().map(input -> stringifyGate(input, !useParentheses)).sorted().toList();
        return STR."\{useParentheses ? '(' : '['}\{inputs.getFirst()} \{gate.getOperation()} \{inputs.getLast()}:\{label}\{useParentheses ? ')' : ']'}";
    }

    public String findFaults() {
        final Gate directCarry0 = gates.values().stream()
                .filter(gate -> gate instanceof AndGate)
                .filter(gate -> gate.hasInput("x00"))
                .findFirst()
                .orElseThrow();
        final Gate directCarry1 = gates.values().stream()
                .filter(gate -> gate instanceof AndGate)
                .filter(gate -> gate.hasInput("x01"))
                .findFirst()
                .orElseThrow();
        final Gate directSum1 = gates.values().stream()
                .filter(gate -> gate instanceof XorGate)
                .filter(gate -> gate.hasInput("x01"))
                .findFirst()
                .orElseThrow();
        directCarries.put(0, directCarry0.getLabel());
        directCarries.put(1, directCarry1.getLabel());
        directSums.put(1, directSum1.getLabel());
        final List<String> outputs = getOutputs().toList().reversed();
        for (int i = 2; i < outputs.size(); i++) {
            mendFaults(outputs.get(i), i);
        }
        return faults.stream()
                .sorted()
                .collect(Collectors.joining(","));
    }

    private void mendFaults(final String output, final int intendedBit) {
        final Gate gate = gates.get(output);
        final List<String> inputs = gate.getInputs();
        final Optional<String> directSumParent = gate.getInputs().stream()
                .filter(label -> isDirectSumForBit(label, intendedBit))
                .findFirst();
        final Optional<String> carryParent = gate.getInputs().stream()
                .filter(label -> isCarryForBit(label, intendedBit))
                .findFirst();
        if (directSumParent.isPresent()) {
            if (carryParent.isPresent()) {
                // All is well
                return;
            }
            // carryParent is crossed
            final String correctCarryParent = findOtherInput(inputs, directSumParent.get());
            carries.put(intendedBit, correctCarryParent);
            faults.add(correctCarryParent);
            return;
        }
        if (carryParent.isPresent()) {
            // directSumParent is crossed
            final String correctDirectSumParent = findOtherInput(inputs, carryParent.get());
            directSums.put(intendedBit, correctDirectSumParent);
            faults.add(correctDirectSumParent);
            return;
        }
        // gate is crossed
        faults.add(output);
    }

    private boolean isCarryForBit(final String labelOfCarry, final int intendedBit) {
        return isGateForBit(labelOfCarry, intendedBit, this::locateCarryForBit, carries);
    }

    private String locateCarryForBit(final String labelOfCarry, final int intendedBit) {
        if (carries.containsKey(intendedBit)) {
            return carries.get(intendedBit);
        }
        // Has parents directCarry intendedBit - 1, previousCarry intendedBit
        final Gate gate = gates.get(labelOfCarry);
        final List<String> inputs = gate.getInputs();
        final Optional<String> directCarryParent = gate.getInputs().stream()
                .filter(label -> isDirectCarryForBit(label, intendedBit - 1))
                .findFirst();
        final Optional<String> previousCarryParent = gate.getInputs().stream()
                .filter(label -> isPreviousCarryForBit(label, intendedBit))
                .findFirst();
        if (directCarryParent.isPresent()) {
            if (previousCarryParent.isPresent()) {
                // All is well
                carries.put(intendedBit, labelOfCarry);
                return labelOfCarry;
            }
            // previousCarryParent is crossed
            final String correctPreviousCarryParent = findOtherInput(inputs, directCarryParent.get());
            previousCarries.put(intendedBit, correctPreviousCarryParent);
            faults.add(correctPreviousCarryParent);
            carries.put(intendedBit, labelOfCarry);
            return labelOfCarry;
        }
        if (previousCarryParent.isPresent()) {
            final String correctDirectCarryParent = findOtherInput(inputs, previousCarryParent.get());
            directCarries.put(intendedBit - 1, correctDirectCarryParent);
            faults.add(correctDirectCarryParent);
            carries.put(intendedBit, labelOfCarry);
            return labelOfCarry;
        }
        final String correctLabel = locateSpecificGateForBit(labelOfCarry, intendedBit, this::isCarryForBit);
        carries.put(intendedBit, correctLabel);
        return correctLabel;
    }

    private boolean isCarryForBit(final Gate gate, final int intendedBit) {
        return gate.isOr()
                && gate.getInputs().stream().anyMatch(label -> isDirectCarryForBit(label, intendedBit - 1))
                && gate.getInputs().stream().anyMatch(label -> isPreviousCarryForBit(label, intendedBit));
    }

    private boolean isPreviousCarryForBit(final String labelOfPreviousCarry, final int intendedBit) {
        return isGateForBit(labelOfPreviousCarry, intendedBit, this::locatePreviousCarryForBit, previousCarries);
    }

    private String locatePreviousCarryForBit(final String labelOfPotentialPreviousCarry, final int intendedBit) {
        if (previousCarries.containsKey(intendedBit)) {
            return previousCarries.get(intendedBit);
        }
        // parents are direct carry n-2 and direct sum n-1
        final Gate gate = gates.get(labelOfPotentialPreviousCarry);
        final List<String> inputs = gate.getInputs();
        final Optional<String> directCarryParent = inputs.stream()
                .filter(label -> isDirectCarryForBit(label, intendedBit - 2))
                .findFirst();
        final Optional<String> directSumParent = inputs.stream()
                .filter(label -> isDirectSumForBit(label, intendedBit - 1))
                .findFirst();
        if (directCarryParent.isPresent()) {
            if (directSumParent.isPresent()) {
                // All is well
                previousCarries.put(intendedBit, labelOfPotentialPreviousCarry);
                return labelOfPotentialPreviousCarry;
            }
            // directSumParent is crossed
            final String correctSumParent = findOtherInput(inputs, directCarryParent.get());
            directSums.put(intendedBit - 1, correctSumParent);
            faults.add(correctSumParent);
            previousCarries.put(intendedBit, labelOfPotentialPreviousCarry);
            return labelOfPotentialPreviousCarry;
        }
        if (directSumParent.isPresent()) {
            // directCarryParent is crossed
            final String correctCarryParent = findOtherInput(inputs, directSumParent.get());
            directCarries.put(intendedBit - 2, correctCarryParent);
            faults.add(correctCarryParent);
            previousCarries.put(intendedBit, labelOfPotentialPreviousCarry);
            return labelOfPotentialPreviousCarry;
        }
        // gate is crossed
        final String correctLabel = locateSpecificGateForBit(labelOfPotentialPreviousCarry, intendedBit, this::isPreviousCarryForBit);
        previousCarries.put(intendedBit, correctLabel);
        return correctLabel;
    }

    private boolean isPreviousCarryForBit(final Gate gate, final int intendedBit) {
        return gate.isAnd()
                && gate.getInputs().stream().anyMatch(label -> isDirectCarryForBit(label, intendedBit - 2))
                && gate.getInputs().stream().anyMatch(label -> isDirectSumForBit(label, intendedBit - 1));
    }

    private boolean isDirectSumForBit(final String label, final int intendedBit) {
        return isGateForBit(label, intendedBit, this::locateDirectSumForBit, directSums);
    }

    private String locateDirectSumForBit(final String labelOfPotentialDirectSum, final int intendedBit) {
        return locateSpecificGateForBit(labelOfPotentialDirectSum, intendedBit, this::isDirectSumForBit);
    }

    private boolean isDirectSumForBit(final Gate gate, final int intendedBit) {
        return gate.isXor() && gate.hasInput(buildDirectInput(intendedBit));
    }

    private boolean isDirectCarryForBit(final String label, final int intendedBit) {
        return isGateForBit(label, intendedBit, this::locateDirectCarryForBit, directCarries);
    }

    private String locateDirectCarryForBit(final String labelOfPotentialDirectCarry, final int intendedBit) {
        return locateSpecificGateForBit(labelOfPotentialDirectCarry, intendedBit, this::isDirectCarryForBit);
    }

    private boolean isDirectCarryForBit(final Gate gate, final int intendedBit) {
        return gate.isAnd() && gate.hasInput(buildDirectInput(intendedBit));
    }

    private static String findOtherInput(final List<String> inputs, final String wrongParent) {
        final String firstInput = inputs.getFirst();
        return firstInput.equals(wrongParent) ? inputs.getLast() : firstInput;
    }

    private boolean isGateForBit(final String label, final int intendedBit, final BiFunction<String, Integer, String> locator, final Map<Integer, String> gatesOfType) {
        if (!gates.containsKey(label)) {
            return false;
        }
        if (!gatesOfType.containsKey(intendedBit)) {
            gatesOfType.put(intendedBit, locator.apply(label, intendedBit));
        }
        return gatesOfType.get(intendedBit).equals(label);
    }

    private String locateSpecificGateForBit(final String labelOfPotentialGate, final int intendedBit, final BiPredicate<Gate, Integer> matcher) {
        final Gate gate = gates.get(labelOfPotentialGate);
        if (gate != null && matcher.test(gate, intendedBit)) {
            return labelOfPotentialGate;
        }
        return gates.values().stream()
                .filter(g -> matcher.test(g, intendedBit))
                .findFirst()
                .map(Gate::getLabel)
                .orElseThrow();
    }

    private static String buildDirectInput(final int intendedBit) {
        return FMT."x%02d\{intendedBit}";
    }

    private boolean canCompute(final Gate gate) {
        return gate.getInputs().stream()
                .allMatch(registers::containsKey);
    }

    private long combineOutput() {
        return getOutputs()
                .map(registers::get)
                .map(value -> value ? 1L : 0L)
                .reduce(0L, (total, number) -> (total << 1) | number);
    }

    private Stream<String> getOutputs() {
        return gates.keySet().stream()
                .filter(label -> label.startsWith("z"))
                .sorted(Comparator.reverseOrder());
    }

    private void setupGate(final String description) {
        final String[] parts = description.split(UtStrings.WHITE_SPACE_REGEX);
        final String firstInput = parts[0];
        final String operation = parts[1];
        final String secondInput = parts[2];
        final String label = parts[4];
        dependencies.computeIfAbsent(firstInput, _ -> new LinkedList<>()).add(label);
        dependencies.computeIfAbsent(secondInput, _ -> new LinkedList<>()).add(label);
        final Gate gate = Gate.fromDescriptors(firstInput, operation, secondInput, label);
        gates.put(label, gate);
    }

    private void setupValue(final String description) {
        final String[] parts = description.split(": ");
        final Boolean value = parts[1].equals("1");
        registers.put(parts[0], value);
    }
}
