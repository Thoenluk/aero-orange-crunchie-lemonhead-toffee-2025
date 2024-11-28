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

import static ch.thoenluk.ut.UtStrings.println;

/**
 *
 * @author Lukas Thöni lukas.thoeni@gmx.ch
 */
public class ChallengeRunner {
    private final static Scanner USER_INPUT = new Scanner(System.in);
    private final static String FIRST_CHALLENGE_SUFFIX = "1";
    private final static String SECOND_CHALLENGE_SUFFIX = "2";
    private File[] challengeFolders;

    public void main(final String[] args) throws Exception {
        println("Scanning for challenge folders...");
        challengeFolders = getChallengeFolders();

        printChallengeFolderNames();

        final int selectedChallenge = getSelectedChallengeFromUser();

        testAndRunChristmasSaver(selectedChallenge);
    }

    private static File[] getChallengeFolders() {
        final File currentFolder = new File(".");
        final File[] challengeFolders = currentFolder.listFiles(ChallengeRunner::isProbablyChallengeFolder);
        if (challengeFolders == null) throw new AssertionError();

        Arrays.sort(challengeFolders, (o1, o2) -> {
            final int number1 = UtParsing.cachedParseInt(o1.getName().split(" ")[0]);
            final int number2 = UtParsing.cachedParseInt(o2.getName().split(" ")[0]);
            return number1 - number2;
        });

        return challengeFolders;
    }

    private void printChallengeFolderNames() {
        println(STR."Found \{challengeFolders.length} challenges: ");
        final StringBuilder output = new StringBuilder();
        for (int i = 0; i < challengeFolders.length; i++) {
            output.append(STR."\{i}:\t \{challengeFolders[i].getName().replaceAll("\\d+\\s+", "")}\n");
        }
        output.append("\nNow choose one.");
        println(output.toString());
    }

    private int getSelectedChallengeFromUser() {
        int selectedChallenge = -1;
        while (selectedChallenge < 0) {
            selectedChallenge = USER_INPUT.nextInt();

            if (selectedChallenge < 0 || challengeFolders.length < selectedChallenge) {
                println("Only and exactly one of the above numbers shalt thou choose.");
                selectedChallenge = -1;
            }
        }
        return selectedChallenge;
    }

    private void testAndRunChristmasSaver(final int selectedChallenge) throws Exception {
        final ChristmasSaver christmasSaver = getChristmasSaverForChallenge(selectedChallenge);
        final File challengeFolder = challengeFolders[selectedChallenge];

        testChristmasSaver(challengeFolder, christmasSaver::saveChristmas, FIRST_CHALLENGE_SUFFIX);

        final File[] actualInputFiles = challengeFolder.listFiles((_, name) -> name.equals("input.txt"));

        if (actualInputFiles == null) throw new AssertionError();
        if (actualInputFiles.length != 1) throw new AssertionError();

        final String input = Files.readString(actualInputFiles[0].toPath());
        println("Determined the result for the first challenge is:");
        long millisBeforeStart = System.currentTimeMillis();
        println(christmasSaver.saveChristmas(input));
        println(STR."And did it in \{System.currentTimeMillis() - millisBeforeStart}ms!");

        println("What fun that was. Running second challenge...");

        testChristmasSaver(challengeFolder, christmasSaver::saveChristmasAgain, SECOND_CHALLENGE_SUFFIX);
        println("Determined the result for the second challenge is:");
        millisBeforeStart = System.currentTimeMillis();
        println(christmasSaver.saveChristmasAgain(input));
        println(STR."And did it in \{System.currentTimeMillis() - millisBeforeStart}ms!");
    }

    // I do not fear what this method does; I fear what kind of further automation I'll think up next year.
    // 2023 update: I was correct to fear.
    private static ChristmasSaver getChristmasSaverForChallenge(final int challenge) {
        final File challengeClassFolder = new File(STR.".\\src\\ch\\thoenluk\\solvers\\challenge\{challenge}");

        if (!challengeClassFolder.isDirectory()) throw new AssertionError();

        return Arrays.stream(Objects.requireNonNull(challengeClassFolder.listFiles(ChallengeRunner::isJavaFile)))
                .map(File::getPath)
                .map(path -> path.substring(6, path.length() - 5).replaceAll("\\\\", "."))
                .map(name -> {
                    try { return Class.forName(name); }
                    catch (final ClassNotFoundException e) { throw new AssertionError(e); }
                })
                .filter(ChristmasSaver.class::isAssignableFrom)
                .map(aClass -> {
                    try {
                        return aClass.asSubclass(ChristmasSaver.class).getConstructor().newInstance();
                    } catch (final ClassCastException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        throw new AssertionError(e);
                    }
                })
                .findFirst()
                .orElseThrow();
    }

    private static boolean isJavaFile(final File file) {
        return file.isFile() && file.getName().endsWith(".java");
    }

    private static void testChristmasSaver(final File challengeFolder, final UnaryOperator<String> savingMethod, final String challengeSuffix) throws IOException {
        final String inputPrefix = STR."test\{challengeSuffix}_input";
        final String outputPrefix = STR."test\{challengeSuffix}_output";

        final File[] testInputs = challengeFolder.listFiles((_, fileName) -> fileName.startsWith(inputPrefix));
        final File[] testOutputs = challengeFolder.listFiles((_, fileName) -> fileName.startsWith(outputPrefix));

        if (testInputs == null) throw new AssertionError();
        if (testOutputs == null) throw new AssertionError();

        if ((testInputs.length != testOutputs.length)) throw new AssertionError();

        Arrays.sort(testInputs);
        Arrays.sort(testOutputs);

        for (int i = 0; i < testInputs.length; i++) {
            final File testInput = testInputs[i];
            final File testOutput = testOutputs[i];

            UtStrings.print(STR."Running test \{testInput.getName()}... ");
            final String testInputString = Files.readString(testInput.toPath());
            final String testOutputString = Files.readString(testOutput.toPath());
            final String actualOutput = savingMethod.apply(testInputString);

            if (!actualOutput.equals(testOutputString)) {
                final String message = STR."""
                        Failed test \{testInput.getName()}
                        Input was:
                        \{testInputString}
                        And expected output was:
                        \{testOutputString}
                        But actual output was:
                        \{actualOutput}""";
                throw new AssertionError(message);
            }

            println(STR."Matched \{testOutput.getName()}");
        }
    }

    private static boolean isProbablyChallengeFolder(final File pathname) {
        return pathname.isDirectory() && pathname.getName().matches("\\d+ .+");
    }
}
