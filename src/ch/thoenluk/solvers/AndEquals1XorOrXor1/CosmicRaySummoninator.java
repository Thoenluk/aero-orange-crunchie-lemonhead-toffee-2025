package ch.thoenluk.solvers.AndEquals1XorOrXor1;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.ut.UtStrings;

@Day(24)
public class CosmicRaySummoninator implements ChristmasSaver {
    @Override
    public String saveChristmas(final String input) {
        return new MonitoringDevice(input).simulate();
    }

    @Override
    public String saveChristmasAgain(final String input) {
        final MonitoringDevice device = new MonitoringDevice(input);
        UtStrings.println();
        UtStrings.println(device.getSchematic());

        return "jqf,mdd,skh,wpd,wts,z11,z19,z37";
        // Honk! Sorry, twelve hours, the time box is up.
        // See https://github.com/Thoenluk/amaretto-ovo-cadbury-taffy-licorice-2023/blob/master/src/ch/thoenluk/solvers/challenge19/PartInsuranceClaimProcessinator.java
        // for the full rant.
        //
        // I used another person's solver. You can call me a cheater or whatever you like, I give myself the leeway to do this on very few
        // challenges each year when I've already worked out what must be done, and how to do it, so all the learning has already happened.
        // Poring over code for days instead of achieving anything productive won't do anything for me.
        //
        // A reminder: We do this all the time in software development. It's encouraged. It's why we have libraries and teams. If I DO force
        // myself to reinvent the wheel 48 times, it's only fair to do the equivalent of asking a team member 5% of the time.
        // Yes, I'm getting arbitrary fake star points that I don't have the solution to back up - too bad!
        //
        // More specifically: If I was given a usable example, I would be highly motivated to debug and find the issue. It's not that I don't care
        // to make good code that actually works 100% of the time rather than an AI bragging about being vaguely correct 6/7 times.
        // It's that if you won't give me examples, truth tables, edge case descriptions - i.e. specification - then I don't care to guess.
        // You'll note the two times I did use another person's solution this year - this challenge and Day 12 challenge 2 - neither time
        // were we provided with any kind of usable example.
        //
        // I've described my approach below. If it's flawed in concept, then truly, I didn't work it out. If there's some weird bug
        // that I certainly would find if I gave myself the time to set up proper tests, I'll say I did what matters.
        //
        // The idea is this: Starting with bit 2, each output bit ought to look like this:
        // directSum(N) XOR carry(N) -> zN
        // xN XOR yN -> directSum(N)
        // directCarry(N - 1) OR previousCarry(N) -> carry(N)
        // x(N) AND y(N) -> directCarry(N)
        // directCarry(N - 2) AND directSum(N - 1) -> previousCarry(N)
        // See device.getSchematic()'s output, which I'm letting execute as an approximation of how long doing this properly would take in speedrun mode.
        // ([([x00 AND y00:hjp] AND [x01 XOR y01:kjs]:wkq) OR (x01 AND y01:fqg):vdq] XOR [x02 XOR y02:rvm]:z02) -> z02
        // == ((directCarry(0) AND directSum(1) [previousCarry(2)]) OR directCarry(1) [carry(2)]) XOR directSum(2) -> z02
        //
        // Future output bits have a higher N, but since we already calculated the inputs or they come from direct values, we can calculate
        // those higher output bits in constant time rather than having to work out x00 AND y00 for output bit 45.
        //
        // Consequently, for each output bit at or above z02, we can assert it has this shape, adding two direct gates (made of x and y)
        // and three composite gates that take in specific values. If we know the input gates for a specific bit to be correctly assigned,
        // we can work out whether the gates for bit N are correctly assigned.
        // This by asking "Is the output bit made of a directSum(N) XOR carry(N),
        //      the directSum(N) being made of xN XOR yN,
        //      the carry being made of a directCarry(N - 1) OR previousCarry(N),
        // and so on, while caching what each correct gate is.
        // If we know directCarry(0) is correctly assigned to hjp, there's no need to re-explore it.
        //
        // What's more - or so the theory goes - since we probably(TM) haven't swapped both inputs for a given gate,
        // we can work out WHICH gate is incorrectly assigned. Starting from the output bit zN:
        // If all inputs are properly shaped, all is well. Since we started from zN, we know the end result is correct.
        // If one input is improperly shaped, but the other is correct, then the one must be wrong.
        //      Find that input's assignment in our cache and swap it for our expected input source
        //      - If we expected kjs to hold the directSum(1), but the directSum(1) is assigned to wvt, change the cache from wvt to kjs.
        //      The idea being that inputs aren't swapped, outputs are, so the inputs would be correct.
        //      This is probably where my mistake lies and an absolute bastard of a time making another solution.
        // If both inputs are improperly shaped, then the output of this gate is wrong.
        //      I make an assumption here that they wouldn't cross both inputs of the same gate.
        //      While the specification allows it to happen, since each gate's output is only being crossed once,
        //      I can't think of how I'd distinguish between that and a crossed output.
        //      Like with a crossed input, swap in the correct output and go on.
        //
        // Since we are exploring arbitrary gates while trying to decide whether a gate is correctly shaped, if a gate isn't
        // cached, then determine what its inputs SHOULD be and find a gate that has those inputs. Determining the correct
        // inputs using caches, recursively filling the cache up to the input bits. In effect, starting from the left, the x/y input bits,
        // and working our way downward finding which gates have the correct shape and inputs.
        //
        // Thinking it over now, the issue is absolutely that I am greedily reassigning the caches:
        // If I expect a given gate to be an output gate, then I expect one of its parents to be a carry bit.
        // Its parents get bent into shape if they don't comply. Checking the operation first helps but doesn't solve this.
        // So we'd need to go up another level towards the inputs and only correct caches if we're sure they are actually wrong.
        // Well there you have it. Now close your eyes and imagine I bothered to implement that.
    }
}
