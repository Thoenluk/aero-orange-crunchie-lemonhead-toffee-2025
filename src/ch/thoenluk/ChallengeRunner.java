package ch.thoenluk;

import ch.thoenluk.ut.UtParsing;
import ch.thoenluk.ut.UtStrings;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static ch.thoenluk.ut.UtStrings.println;

/**
 * @author Lukas Thöni lukas.thoeni@gmx.ch
 */
public class ChallengeRunner {
    private final static Scanner USER_INPUT = new Scanner(System.in);
    private final static String FIRST_CHALLENGE_SUFFIX = "1";
    private final static String SECOND_CHALLENGE_SUFFIX = "2";
    public static final int NANO_TO_MILLI = 1_000_000;
    private final List<? extends Class<? extends ChristmasSaver>> christmasSaverClasses = findChristmasSaverClasses();
    private Supplier<int[]> daySelectionStrategy = this::getSelectedChallengeFromUser;
    private Function<Integer, Long> executionStrategy = this::testAndRunChristmasSaver;
    private int[] argsChallenges = null;

    public void main(final String[] args) {
        printChallengeFolderNames();
        pickStrategies(args);
        executeChristmasSavers();
    }

    private void printChallengeFolderNames() {
        println("Scanning for challenge folders...");
        println(STR."Found \{christmasSaverClasses.size()} challenges: ");
        final StringBuilder output = new StringBuilder();
        for (int i = 0; i < christmasSaverClasses.size(); i++) {
            output.append(STR."\{i}:\t \{getChallengeFolderName(christmasSaverClasses.get(i))}\n");
        }
        println(output.toString());
    }

    private String getChallengeFolderName(final Class<? extends ChristmasSaver> christmasSaverClass) {
        final String fullPackageName = christmasSaverClass.getPackage().getName();
        return fullPackageName.substring(fullPackageName.lastIndexOf('.') + 1);
    }

    private void pickStrategies(final String[] args) {
        final String flag;
        if (args.length == 0) {
            println("No flags detected. Proceeding with default. Btw: You can use these flags: 'default', 'latest', 'latest-second-only', 'speedrun', 'challenges'.\n" +
                    "For 'challenges', also give the challenges you want in a space-separated list.");
            flag = "default";
        } else {
            flag = args[0];
        }
        switch (flag) {
            case "default":
                daySelectionStrategy = this::getSelectedChallengeFromUser;
                executionStrategy = this::testAndRunChristmasSaver;
                break;
            case "latest":
                daySelectionStrategy = this::selectMostRecentChallenge;
                executionStrategy = this::testAndRunChristmasSaver;
                break;
            case "latest-second-only":
                daySelectionStrategy = this::selectMostRecentChallenge;
                executionStrategy = this::onlyTestAndRunSecondChallenge;
                break;
            case "speedrun":
                daySelectionStrategy = this::selectAllChallenges;
                executionStrategy = this::onlyRunChristmasSaver;
                break;
            case "challenges":
                daySelectionStrategy = this::selectChallengesFromArgs;
                executionStrategy = this::testAndRunChristmasSaver;
                argsChallenges = Arrays.stream(args, 1, args.length)
                        .mapToInt(UtParsing::cachedParseInt)
                        .toArray();
                break;
        }
    }

    private void executeChristmasSavers() {
        final int[] challenges = daySelectionStrategy.get();
        final long executionTime = Arrays.stream(challenges)
                .mapToObj(executionStrategy::apply)
                .reduce(Long::sum)
                .orElseThrow();
        println(STR."Took a total of \{executionTime / NANO_TO_MILLI}ms for it all!");
    }

    private int[] selectMostRecentChallenge() {
        println("Proceeding with highest challenge.");
        return new int[]{christmasSaverClasses.size() - 1};
    }

    private int[] selectAllChallenges() {
        println("Proceeding with all challenges.");
        return IntStream.range(0, christmasSaverClasses.size()).toArray();
    }

    private int[] selectChallengesFromArgs() {
        println(STR."Proceeding with challenges given as args: \{argsChallenges}");
        return argsChallenges;
    }

    private int[] getSelectedChallengeFromUser() {
        println("Now choose one.");
        int selectedChallenge = -1;
        while (selectedChallenge < 0) {
            selectedChallenge = USER_INPUT.nextInt();

            if (selectedChallenge < 0 || christmasSaverClasses.size() < selectedChallenge) {
                println("Only and exactly one of the above numbers shall you choose.");
                selectedChallenge = -1;
            }
        }
        return new int[]{selectedChallenge};
    }

    private long testAndRunChristmasSaver(final int selectedChallenge) {
        final ChristmasSavingPackage result = wrapChristmasSavingPackage(selectedChallenge);

        println("Running first challenge...");
        testChristmasSaver(result.challengeFolder(), result.christmasSaver()::saveChristmas, FIRST_CHALLENGE_SUFFIX);
        final long timeForFirst = runChristmasSaver(result.christmasSaver()::saveChristmas, result.input());
        println("What fun that was. Running second challenge...");
        testChristmasSaver(result.challengeFolder(), result.christmasSaver()::saveChristmasAgain, SECOND_CHALLENGE_SUFFIX);
        return timeForFirst + runChristmasSaver(result.christmasSaver()::saveChristmasAgain, result.input());
    }

    private long onlyRunChristmasSaver(final int selectedChallenge) {
        final ChristmasSavingPackage result = wrapChristmasSavingPackage(selectedChallenge);
        return runChristmasSaver(result.christmasSaver()::saveChristmas, result.input()) +
        runChristmasSaver(result.christmasSaver()::saveChristmasAgain, result.input());
    }

    private long onlyTestAndRunSecondChallenge(final int selectedChallenge) {
        final ChristmasSavingPackage result = wrapChristmasSavingPackage(selectedChallenge);
        testChristmasSaver(result.challengeFolder(), result.christmasSaver()::saveChristmasAgain, SECOND_CHALLENGE_SUFFIX);
        return runChristmasSaver(result.christmasSaver()::saveChristmasAgain, result.input());
    }

    private ChristmasSavingPackage wrapChristmasSavingPackage(final int selectedChallenge) {
        // Wrap as in wrapping paper AND as in object.
        // I only intended the wrapping paper meaning and I too hate this unintentional pun.
        final Class<? extends ChristmasSaver> christmasSaverClass = christmasSaverClasses.get(selectedChallenge);
        final ChristmasSaver christmasSaver = instantiateChristmasSaver(christmasSaverClass);
        final Package christmasSaverClassPackage = christmasSaverClass.getPackage();
        final File challengeFolder = new File(STR."src\\\{christmasSaverClassPackage.getName().replaceAll("\\.", "\\\\")}");

        final String input = findActualInput(challengeFolder);
        return new ChristmasSavingPackage(christmasSaver, challengeFolder, input);
    }

    private static String findActualInput(final File challengeFolder) {
        final File[] actualInputFiles = challengeFolder.listFiles((_, name) -> name.equals("input.txt"));

        if (actualInputFiles == null) throw new AssertionError();
        if (actualInputFiles.length != 1) throw new AssertionError();

        return UtStrings.readFile(actualInputFiles[0]);
    }

    private static long runChristmasSaver(final UnaryOperator<String> savingMethod, final String input) {
        println("Determined the result for the challenge is:");
        final long nanosBeforeStart = System.nanoTime();
        println(savingMethod.apply(input));
        final long executionTime = System.nanoTime() - nanosBeforeStart;
        println(STR."And did it in \{executionTime / NANO_TO_MILLI}ms!");
        return executionTime;
    }

    // I do not fear what this method does; I fear what kind of further automation I'll think up next year.
    // 2023 update: I was correct to fear.
    // 2024 update: I suffered a dream of steel and oil. Of a hellscape of good intentions - to pre-parse input based on the
    //              desired input type as declared in an annotation. In my fervor to create a lazier program, saving one
    //              entire method call per saveChristmas, I had made a hellscape of Reflection and Object.
    //              I awoke when I realised that while it certainly worked, it removed all premise of type-safety, nay,
    //              even the concept of calling a method on an object rather than passing that object as a parameter.
    //              I abandoned the concept. Types are our greatest asset in Java. Some experiments should not be done.
    // Next day update: That being said, I didn't say I was NOT going to get lazier with annotations and make a nightmare
    //                  of Reflection and possibly steel and oil. More wine, yum yum!
    //                  I could package the classes into a record including their package; That'd remove the admittedly
    //                  less-than-guaranteed operation in wrapChristmasSavingPackage, because there is little guarantee
    //                  a class was actually loaded from the file system. But in this case, there is, because I'm scanning
    //                  the file system to begin with. And I wanted to know if I COULD find the folder from the class.
    private static List<? extends Class<? extends ChristmasSaver>> findChristmasSaverClasses() {
        try (final Stream<Path> paths = Files.find(Paths.get(".", "src", "ch", "thoenluk", "solvers"),
                Integer.MAX_VALUE,
                ChallengeRunner::isJavaFile)) {
            return paths.map(Path::toString)
                    .map(ChallengeRunner::toSearchableFilePath)
                    .map(filePath -> {
                        try {
                            return Class.forName(filePath);
                        } catch (final ClassNotFoundException e) {
                            throw new AssertionError(e);
                        }
                    })
                    .filter(ChristmasSaver.class::isAssignableFrom)
                    .map(christmasSaverClass -> (Class<? extends ChristmasSaver>) christmasSaverClass.asSubclass(ChristmasSaver.class))
                    .sorted(Comparator.comparing(ChallengeRunner::findDayValue))
                    .toList();
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }

    private static boolean isJavaFile(final Path path, final BasicFileAttributes attrs) {
        return path.toString().endsWith(".java");
    }

    private static String toSearchableFilePath(final String filePath) {
        return filePath.substring(6, filePath.length() - 5).replaceAll("\\\\", ".");
    }

    private static int findDayValue(final Class<?> christmasSaverClass) {
        return Optional.ofNullable(christmasSaverClass.getAnnotation(Day.class))
                .map(Day::value)
                .orElse(-1);
    }

    private static ChristmasSaver instantiateChristmasSaver(final Class<? extends ChristmasSaver> christmasSaverClass) {
        try {
            return christmasSaverClass.getConstructor().newInstance();
        } catch (final ClassCastException | InstantiationException | IllegalAccessException |
                       InvocationTargetException | NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    private static void testChristmasSaver(final File challengeFolder, final UnaryOperator<String> savingMethod, final String challengeSuffix) {
        final String inputPrefix = STR."test\{challengeSuffix}_input";
        final String outputPrefix = STR."test\{challengeSuffix}_output";

        final File[] testInputs = challengeFolder.listFiles((_, fileName) -> fileName.startsWith(inputPrefix));
        final File[] testOutputs = challengeFolder.listFiles((_, fileName) -> fileName.startsWith(outputPrefix));

        if (testInputs == null) throw new AssertionError();
        if (testOutputs == null) throw new AssertionError();

        if ((testInputs.length != testOutputs.length)) throw new AssertionError();

        Arrays.sort(testInputs);
        Arrays.sort(testOutputs);

        try (final StructuredTaskScope.ShutdownOnFailure scope = new StructuredTaskScope.ShutdownOnFailure()) {
            for (int i = 0; i < testInputs.length; i++) {
                final File testInput = testInputs[i];
                final File testOutput = testOutputs[i];
                scope.fork(() -> executeTestCase(savingMethod, testInput, testOutput));
            }
            scope.join();
            scope.throwIfFailed();
        } catch (final InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static int executeTestCase(final UnaryOperator<String> savingMethod, final File testInput, final File testOutput) {
        UtStrings.print(STR."Running test \{testInput.getName()}... ");
        final String testInputString = UtStrings.readFile(testInput);
        final String testOutputString = UtStrings.readFile(testOutput);
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
        return 0; // Forked processes must return something, but we don't care about the return - if we fail, we throw!
    }

    private record ChristmasSavingPackage(ChristmasSaver christmasSaver, File challengeFolder, String input) {
    }
}
