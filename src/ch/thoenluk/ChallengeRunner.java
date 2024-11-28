package ch.thoenluk;
import ch.thoenluk.ut.UtParsing;
import ch.thoenluk.ut.UtStrings;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.UnaryOperator;

/**
 *
 * @author Lukas Thöni lukas.thoeni@gmx.ch
 */
public class ChallengeRunner {


    private final static Scanner USER_INPUT = new Scanner(System.in);
    private final static String FIRST_CHALLENGE_SUFFIX = "1";
    private final static String SECOND_CHALLENGE_SUFFIX = "2";

    public static void main(String[] args) throws Exception {
        UtStrings.println("Scanning for challenge folders...");
        final File[] challengeFolders = getChallengeFolders();

        printChallengeFolderIndices(challengeFolders);

        final int selectedChallenge = getSelectedChallengeFromUser(challengeFolders.length);

        testAndRunChristmasSaver(challengeFolders[selectedChallenge], selectedChallenge);
    }

    private static File[] getChallengeFolders() {
        final File currentFolder = new File(".");
        final File[] challengeFolders = currentFolder.listFiles(pathname -> pathname.isDirectory() && pathname.getName().matches("\\d+ .+"));
        if (challengeFolders == null) throw new AssertionError();

        Arrays.sort(challengeFolders, (o1, o2) -> {
            final int number1 = UtParsing.cachedParseInt(o1.getName().split(" ")[0]);
            final int number2 = UtParsing.cachedParseInt(o2.getName().split(" ")[0]);
            return number1 - number2;
        });

        return challengeFolders;
    }

    private static void printChallengeFolderIndices(File[] challengeFolders) {
        UtStrings.println("Found " + challengeFolders.length + " challenges: ");
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < challengeFolders.length; i++) {
            output.append(i).append(":\t").append(challengeFolders[i].getName().replaceAll("\\d+\\s+", "")).append("\n");
        }
        output.append("\n").append("Now choose one.");
        UtStrings.println(output.toString());
    }

    private static int getSelectedChallengeFromUser(int highestPossibleChallenge) {
        int selectedChallenge = -1;
        while (selectedChallenge < 0) {
            selectedChallenge = USER_INPUT.nextInt();

            if (selectedChallenge < 0 || highestPossibleChallenge < selectedChallenge) {
                UtStrings.println("Only and exactly one of the above numbers shalt thou choose.");
                selectedChallenge = -1;
            }
        }
        return selectedChallenge;
    }

    private static void testAndRunChristmasSaver(File challengeFolder, int selectedChallenge) throws Exception {
        final ChristmasSaver christmasSaver = getChristmasSaverForChallenge(selectedChallenge);

        testChristmasSaver(challengeFolder, christmasSaver::saveChristmas, FIRST_CHALLENGE_SUFFIX);

        final File[] actualInputFiles = challengeFolder.listFiles((dir, name) -> name.equals("input.txt"));

        if (actualInputFiles == null) throw new AssertionError();
        if (actualInputFiles.length != 1) throw new AssertionError();

        String input = Files.readString(actualInputFiles[0].toPath());
        UtStrings.println("Determined the result for the first challenge is:");
        long millisBeforeStart = System.currentTimeMillis();
        UtStrings.println(christmasSaver.saveChristmas(input));
        UtStrings.println("And did it in " + (System.currentTimeMillis() - millisBeforeStart) + "ms!");

        UtStrings.println("What fun that was. Running second challenge...");

        testChristmasSaver(challengeFolder, christmasSaver::saveChristmasAgain, SECOND_CHALLENGE_SUFFIX);
        UtStrings.println("Determined the result for the second challenge is:");
        millisBeforeStart = System.currentTimeMillis();
        UtStrings.println(christmasSaver.saveChristmasAgain(input));
        UtStrings.println("And did it in " + (System.currentTimeMillis() - millisBeforeStart) + "ms!");
    }

    // I do not fear what this method does; I fear what kind of further automation I'll think up next year.
    // 2023 update: I was correct to fear.
    private static ChristmasSaver getChristmasSaverForChallenge(int challenge) {
        final File challengeClassFolder = new File(".\\src\\ch\\thoenluk\\solvers\\challenge" + challenge);

        if (!challengeClassFolder.isDirectory()) throw new AssertionError();

        return Arrays.stream(Objects.requireNonNull(challengeClassFolder.listFiles(file -> file.isFile() && file.getName().endsWith(".java"))))
                .map(File::getPath)
                .map(path -> path.substring(6, path.length() - 5).replaceAll("\\\\", "."))
                .map(name -> {
                    try { return Class.forName(name); }
                    catch (ClassNotFoundException e) { throw new AssertionError(e); }
                })
                .filter(ChristmasSaver.class::isAssignableFrom)
                .map(aClass -> {
                    try {
                        return aClass.asSubclass(ChristmasSaver.class).getConstructor().newInstance();
                    } catch (ClassCastException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        throw new AssertionError(e);
                    }
                })
                .findFirst()
                .orElseThrow();
    }

    private static void testChristmasSaver(File challengeFolder, UnaryOperator<String> savingMethod, String challengeSuffix) throws IOException {
        final String inputPrefix = "test" + challengeSuffix + "_input";
        final String outputPrefix = "test" + challengeSuffix + "_output";

        final File[] testInputs = challengeFolder.listFiles((File dir, String fileName) -> fileName.startsWith(inputPrefix));
        final File[] testOutputs = challengeFolder.listFiles((File dir, String fileName) -> fileName.startsWith(outputPrefix));

        if (testInputs == null) throw new AssertionError();
        if (testOutputs == null) throw new AssertionError();

        if ((testInputs.length != testOutputs.length)) throw new AssertionError();

        Arrays.sort(testInputs);
        Arrays.sort(testOutputs);

        for (int i = 0; i < testInputs.length; i++) {
            final File testInput = testInputs[i];
            final File testOutput = testOutputs[i];

            UtStrings.print("Running test " + testInput.getName() + "... ");
            final String testInputString = Files.readString(testInput.toPath());
            final String testOutputString = Files.readString(testOutput.toPath());
            final String actualOutput = savingMethod.apply(testInputString);

            if (!actualOutput.equals(testOutputString)) {
                StringBuilder message = new StringBuilder();
                message.append("Failed test ").append(testInput.getName()).append("!\n")
                        .append("Input was:\n")
                        .append(testInputString).append("\n\n")
                        .append("And expected output was:\n")
                        .append(testOutputString).append("\n\n")
                        .append("But actual output was:\n")
                        .append(actualOutput);
                UtStrings.println(message);
                throw new AssertionError();
            }

            UtStrings.println("Matched " + testOutput.getName());
        }
    }
}
