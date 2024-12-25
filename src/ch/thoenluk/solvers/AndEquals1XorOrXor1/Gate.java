package ch.thoenluk.solvers.AndEquals1XorOrXor1;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

abstract class Gate implements BinaryOperator<Boolean> {
    public static final String AND = "AND";
    public static final String OR = "OR";
    public static final String XOR = "XOR";

    private final List<String> inputs;
    private final String operation;
    private final String label;

    public static Gate fromDescriptors(final String firstInput, final String operation, final String secondInput, final String label) {
        return switch (operation) {
            case AND -> new AndGate(firstInput, secondInput, operation, label);
            case OR -> new OrGate(firstInput, secondInput, operation, label);
            case XOR -> new XorGate(firstInput, secondInput, operation, label);
            default -> throw new IllegalStateException(STR."Unexpected value: \{operation}");
        };
    }

    protected Gate(final String firstInput, final String secondInput, final String operation, final String label) {
        this.inputs = Stream.of(firstInput, secondInput).sorted().toList();
        this.operation = operation;
        this.label = label;
    }

    public boolean compute(final List<Boolean> inputValues) {
        return apply(inputValues.getFirst(), inputValues.getLast());
    }

    public boolean hasInput(final String input) {
        return inputs.contains(input);
    }

    public List<String> getInputs() {
        return inputs;
    }

    public String getOperation() {
        return operation;
    }

    public String getLabel() {
        return label;
    }

    public boolean isXor() {
        return false;
    }

    public boolean isAnd() {
        return false;
    }

    public boolean isOr() {
        return false;
    }
}
