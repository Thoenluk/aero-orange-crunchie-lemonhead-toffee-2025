package ch.thoenluk.solvers.DontImplementAnotherLanguageIBegYou;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.ut.UtStrings;

@Day(17)
public class QuantumRealmNavigationPlanninator implements ChristmasSaver {
    @Override
    public String saveChristmas(final String input) {
        return new Computer(input).execute();
    }

    @Override
    public String saveChristmasAgain(final String input) {
        final Computer computer = new Computer(input);
        final long inputCopyingProgram = computer.findInputToCopyProgram();
        computer.flashMemory(inputCopyingProgram);
        UtStrings.println(STR."Found the correct input (in the next line). To prove it, here's your program (or not, because this challenge soft mandates input hacking): \{computer.execute()}");
        return Long.toString(inputCopyingProgram);
    }
}
