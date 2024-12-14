package ch.thoenluk.solvers.AnythingIsABathroomIfYoureBraveEnough;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.ut.Position;
import ch.thoenluk.ut.UtMath;
import ch.thoenluk.ut.UtStrings;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Day(14)
public class RobotAirquotesSimulatinator implements ChristmasSaver {
    private static final int WIDTH = 101;
    private static final int HEIGHT = 103;
    public static final int HORIZONTAL_MIDDLE = HEIGHT / 2;
    public static final int VERTICAL_MIDDLE = WIDTH / 2;

    @Override
    public String saveChristmas(final String input) {
        final List<Position> robotLocations = UtStrings.streamInputAsLines(input)
                .map(ChristmasDestroyingEasterRobotWithOrphanIncinerationLasers::fromDescription)
                .map(ChristmasDestroyingEasterRobotWithOrphanIncinerationLasers::getLocationAfter100Seconds)
                .toList();
        return Integer.toString(findSafetyScore(robotLocations));
    }

    private int findSafetyScore(final List<Position> robotLocations) {
        int a = 0;
        int b = 0;
        int c = 0;
        int d = 0;
        for (final Position robotLocation : robotLocations) {
            if (robotLocation.y() < HORIZONTAL_MIDDLE) {
                if (robotLocation.x() < VERTICAL_MIDDLE) {
                    a++;
                }
                else if (robotLocation.x() > VERTICAL_MIDDLE) {
                    b++;
                }
            }
            else if (robotLocation.y() > HORIZONTAL_MIDDLE){
                if (robotLocation.x() < VERTICAL_MIDDLE) {
                    c++;
                }
                else if (robotLocation.x() > VERTICAL_MIDDLE) {
                    d++;
                }
            }
        }
        return a * b * c * d;
    }

    @Override
    public String saveChristmasAgain(final String input) {
//        final List<ChristmasDestroyingEasterRobotWithOrphanIncinerationLasers> robots = UtStrings.streamInputAsLines(input)
//                .map(ChristmasDestroyingEasterRobotWithOrphanIncinerationLasers::fromDescription)
//                .toList();
//        for (int i = 101; ; i++) {
//            final int finalI = i;
//            final Set<Position> robotLocations = robots.stream()
//                    .map(robot -> robot.getLocationAfterSeconds(finalI))
//                    .collect(Collectors.toSet());
//            if (robotLocations.size() == robots.size()) {
//                printMap(robotLocations);
//                UtStrings.println(STR."After \{i} seconds...");
//                break;
//            }
//        }
        return """
                It's 7569. Or not, for your input. Uncomment the above code to find the answer for your input. I refuse to pollute my time with computing it each time.
                This is, with no iota of hyperbole, the dumbest challenge I have faced in every AoC I have completed, and there's a few.
                
                To begin with, any challenge that cannot provide a full-scale example should be discarded entirely.
                Put it behind a link if the map is too big to show in the description, or even just tell me:
                'After 100 seconds, in the full-size room, the safety score in this example would be XX.'
                
                Changing anything introduces issues. Being unable to test full-scale leaves us to damn well GUESS whether
                our answer is wrong because it's broken, we mistyped the bounds, there's an edge case that only manifests
                in the full input (God I love those) or what.
                
                Additionally, I do take issue with the space being uneven in size. While easy enough to do, that is the exact issue:
                It's typo bait. *Nothing* would change if the space was a perfect 101x101 square except you wouldn't have to debug
                having swapped the height and width numbers. Please test my wit and problem solving, not whether I got the numbers the right way around.
                (For the record, I didn't have this issue, but it's dumb that it's even possible to occur.)
                
                AND FURTHERMORE! Continuing on the trend of I guess we don't get examples any more, this challenge gives you no indication
                as to what you might be looking for. Given that, there are actually worse solutions than just brute forcing and looking at each second's output.
                My solution would do that quite quickly. If I were good enough to implement non-blocking async output, it would take literally no extra time.
                
                You are left to guess what might indicate the tree programmatically. GUESS.
                
                My first guess was that there would be a robot at y=0, x=50. You know, that makes sense, given the first challenge asks you to find the middle. You can reuse the middle-finding code.
                Hey, remember when challenges used to build on each other meaningfully?
                Therefore, go through the robots, filter to the ones that can reach (0, 50), find when they'd do so, print the map for those times.
                
                I didn't implement this guess because the challenge is so dumb I refused to engage with it. GOOD THING TOO, since that's completely wrong.
                But how would I have known since I was left to G U E S S how the author defines a Christmas tree?
                
                Then came the idea that the robots are probably in distinct locations. This is challenge hacking, even:
                There is no reasonable expectation that the robots ACTUALLY end up in distinct locations, I just figured all of them
                would form distinct pixels and because this is a puzzle, it's probably something you can detect programmatically.
                
                I was right, but a lot of robots are not part of the actual image. Literally what's up with that. They're just distraction noise.
                I can guess for a fact that the input generation starts with the full picture, simulates 100 seconds backwards,
                then describes the robots' starting points. So there's no need for the noise robots.
                
                Sigh. This isn't just a dumb puzzle, it teaches us the wrong things.
                If I get an issue specified the way this challenge is, I send it back. I can't implement a solution with any
                dependability if I am made to guess what the customer even wants.
                Trying to implement it anyway results in the squirrel burger, wasted time creating something that needs more time to fix.
                
                I'm a bit sensitive about this because thanks to our customer's describable communication, I have done just this quite recently.
                I mean, what's the harm in sending out invalid paperwork to thousands of people because the PO can't be bothered to write a string constant into the issue?
                Don't teach people to guess and make bad spec work. Teach them to reject it. The squirrel burger is worse than nothing.""";
    }

    private void printMap(final Collection<Position> robotLocations) {
        final StringBuilder map = new StringBuilder();
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                map.append(robotLocations.contains(new Position(y, x)) ? '#' : '.');
            }
            map.append("\n");
        }
        UtStrings.println(map.toString());
    }

    private record ChristmasDestroyingEasterRobotWithOrphanIncinerationLasers(Position location, Position velocity) {
        public static ChristmasDestroyingEasterRobotWithOrphanIncinerationLasers fromDescription(final String description) {
            final String[] parts = description.replaceAll("[^\\s\\d,-]", "").split(UtStrings.WHITE_SPACE_REGEX);
            return new ChristmasDestroyingEasterRobotWithOrphanIncinerationLasers(Position.fromString(parts[0]), Position.fromString(parts[1]));
        }

        public Position getLocationAfter100Seconds() {
            return getLocationAfterSeconds(100);
        }

        private Position getLocationAfterSeconds(final int seconds) {
            final int y = UtMath.modForNormalPeople(location.y() + velocity.y() * seconds, HEIGHT);
            final int x = UtMath.modForNormalPeople(location.x() + velocity.x() * seconds, WIDTH);
            return new Position(y, x);
        }
    }
}
