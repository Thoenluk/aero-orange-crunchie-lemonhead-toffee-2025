package ch.thoenluk.solvers.DontImplementAnotherLanguageIBegYou;

import ch.thoenluk.ut.UtParsing;
import ch.thoenluk.ut.UtStrings;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

// BWAAAAAAAA
public class Computer {
    private static final int LITERAL_COMBO_OPERAND_BOUND = 3;
    private static final int COMBO_REGISTER_A = 4;
    private static final int COMBO_REGISTER_B = 5;
    private static final int COMBO_REGISTER_C = 6;
    private static final int ADV = 0;
    private static final int BXL = 1;
    private static final int BST = 2;
    private static final int JNZ = 3;
    private static final int BXC = 4;
    private static final int OUT = 5;
    private static final int BDV = 6;
    private static final int CDV = 7;

    private final List<Integer> program;
    private final List<Instruction> instructions;
    private long registerA;
    private long registerB;
    private long registerC;
    private int instructionPointer;
    private final List<Long> output = new LinkedList<>();

    public Computer(final String description) {
        final String[] parts = UtStrings.splitStringWithEmptyLines(description);

        final List<Integer> registers = UtParsing.multilineStringToIntegerList(parts[0].replaceAll("[^\\d\n]", ""));
        flashMemory(registers.getFirst());

        program = new LinkedList<>(UtParsing.commaSeparatedStringToIntegerList(parts[1].replaceAll("[^\\d,]", "")));
        instructions = new ArrayList<>(program.size() / 2);
        for (int i = 0; i < program.size(); i += 2) {
            final int opcode = program.get(i);
            final int operand = program.get(i + 1);
            instructions.add(toInstruction(opcode, operand));
        }
    }

    public String execute() {
        final int size = instructions.size();
        while (instructionPointer < size) {
            instructions.get(instructionPointer).execute();
            instructionPointer++;
        }
        return formatOutput();
    }

    public void flashMemory(final long registerA) {
        this.registerA = registerA;
        registerB = 0;
        registerC = 0;
        instructionPointer = 0;
        output.clear();
    }

    public long findInputToCopyProgram() {
        return findInputForOutput(0L, program.reversed());
    }

    private long findInputForOutput(final long inputSoFar, final List<Integer> outputs) {
        if (outputs.isEmpty()) {
            return inputSoFar;
        }
        final int code = outputs.getFirst();
        final long inputToA = inputSoFar << 3;
        final List<Integer> possibleAdditions = findInputsToCopyCode(code, inputToA);
        for (final int addition : possibleAdditions) {
            final long inputForOutput = findInputForOutput(inputToA + addition, outputs.subList(1, outputs.size()));
            if (inputForOutput != -1) {
                return inputForOutput;
            }
        }
        return -1;
    }

    private List<Integer> findInputsToCopyCode(final int code, final long inputToA) {
        final List<Integer> result = new LinkedList<>();
        for (int addition = 0; addition < 8; addition++) {
            long b = addition ^ 1;
            final long c = (inputToA + addition) >> b;
            b ^= c;
            b ^= 4;
            if (b % 8 == code) {
                result.add(addition);
            }
        }
        return result;
    }

    private String formatOutput() {
        final StringBuilder builder = new StringBuilder();
        output.forEach(number -> builder.append(number).append(','));
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    private Instruction toInstruction(final int opcode, final int operand) {
        return switch (opcode) {
            case ADV -> createComboInstruction(this::adv, operand);
            case BXL -> new LiteralInstruction(this::bxl, operand);
            case BST -> createComboInstruction(this::bst, operand);
            case JNZ -> new LiteralInstruction(this::jnz, operand);
            case BXC -> new LiteralInstruction(this::bxc, operand);
            case OUT -> createComboInstruction(this::out, operand);
            case BDV -> createComboInstruction(this::bdv, operand);
            case CDV -> createComboInstruction(this::cdv, operand);
            default -> throw new IllegalStateException(STR."Unexpected value: \{opcode}");
        };
    }

    private Instruction createComboInstruction(final Consumer<Long> method, final int operand) {
        if (isLiteral(operand)) {
            return new LiteralInstruction(method, operand);
        }
        return new ComboInstruction(method, resolveComboOperand(operand));
    }

    private boolean isLiteral(final int comboOperand) {
        return comboOperand <= LITERAL_COMBO_OPERAND_BOUND;
    }

    private Supplier<Long> resolveComboOperand(final int comboOperand) {
        return switch (comboOperand) {
            case COMBO_REGISTER_A -> this::getRegisterA;
            case COMBO_REGISTER_B -> this::getRegisterB;
            case COMBO_REGISTER_C -> this::getRegisterC;
            default -> throw new IllegalStateException(STR."Unexpected value: \{comboOperand}");
        };
    }

    private void adv(final long operand) {
        setRegisterA(divideA(operand));
    }

    private long divideA(final long operand) {
        return getRegisterA() / (1L << operand);
    }

    private void bxl(final long operand) {
        setRegisterB(getRegisterB() ^ operand);
    }

    private void bst(final long operand) {
        setRegisterB(operand % 8);
    }

    private void jnz(final long operand) {
        if (getRegisterA() == 0) {
            return;
        }
        setInstructionPointer((int) (operand / 2) - 1); // One -2 saves many ifs. Also, they can't tell me NOT to increment the counter, I'm a rebel!
    }

    private void bxc(final long operand) {
        setRegisterB(getRegisterB() ^ getRegisterC());
    }

    private void out(final long operand) {
        output.add(operand % 8);
    }

    private void bdv(final long operand) {
        setRegisterB(divideA(operand));
    }

    private void cdv(final long operand) {
        setRegisterC(divideA(operand));
    }

    private long getRegisterA() {
        return registerA;
    }

    private void setRegisterA(final long registerA) {
        this.registerA = registerA;
    }

    private long getRegisterB() {
        return registerB;
    }

    private void setRegisterB(final long registerB) {
        this.registerB = registerB;
    }

    private long getRegisterC() {
        return registerC;
    }

    private void setRegisterC(final long registerC) {
        this.registerC = registerC;
    }

    private void setInstructionPointer(final int instructionPointer) {
        this.instructionPointer = instructionPointer;
    }

    private record LiteralInstruction(Consumer<Long> method, long value) implements Instruction {
        @Override
        public void execute() {
            method.accept(value);
        }
    }

    private record ComboInstruction(Consumer<Long> method, Supplier<Long> supplier) implements Instruction {
        @Override
        public void execute() {
            method.accept(supplier.get());
        }
    }
}
