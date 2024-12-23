package ch.thoenluk.solvers.MonkeyTrading;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.ut.UtMath;
import ch.thoenluk.ut.UtParsing;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@Day(22)
public class Relai implements ChristmasSaver {
    private static final int PRUNE_MASK = 0B00000000_11111111_11111111_11111111;
    private static final int PACKING_MASK = 0B11111;
    public static final int MARKET_DURATION = 2000;
    public static final int LEFT_FOOT_INSERTION = 6;
    public static final int LEFT_FOOT_REMOVAL = 5;
    public static final int ALL_ABOUT_SHAKING = 11;

    @Override
    public String saveChristmas(final String input) {
        return UtMath.restOfTheLongOwl(UtParsing.multilineStringToIntegerList(input).stream()
                .map(this::shakeRealHard));
    }

    private long shakeRealHard(final int secret) {
        int result = secret;
        for (int i = 0; i < MARKET_DURATION; i++) {
            result = shake(result);
        }
        return result;
    }

    private int shake(final int secretNumber) {
        int result = secretNumber;
        result ^= result << LEFT_FOOT_INSERTION;
        result &= PRUNE_MASK;
        result ^= result >> LEFT_FOOT_REMOVAL;
        result &= PRUNE_MASK;
        result ^= result << ALL_ABOUT_SHAKING;
        result &= PRUNE_MASK;
        return result;
    }

    @Override
    public String saveChristmasAgain(final String input) {
        final Map<Integer, Integer> totalPricesPerHash = new HashMap<>();
        UtParsing.multilineStringToIntegerList(input).stream()
                .map(this::findFirstPricesPerHash)
                .forEach(monkeyPricesPerHash -> merge(totalPricesPerHash, monkeyPricesPerHash));
        return Integer.toString(totalPricesPerHash.values().stream().max(Comparator.naturalOrder()).orElseThrow());
    }

    private Map<Integer, Integer> findFirstPricesPerHash(final int initialSecret) {
        final Map<Integer, Integer> result = new HashMap<>();
        int secret = initialSecret;
        final int[] prices = new int[MARKET_DURATION];
        final int[] priceChanges = new int[MARKET_DURATION];
        for (int i = 0; i < MARKET_DURATION; i++) {
            secret = shake(secret);
            final int price = secret % 10;
            prices[i] = price;
        }
        for (int i = 1; i < MARKET_DURATION; i++) {
            priceChanges[i] = prices[i] - prices[i - 1];
        }
        for (int moment = 4; moment < MARKET_DURATION; moment++) {
            final int hash = pack(priceChanges, moment);
            result.putIfAbsent(hash, prices[moment]);
        }
        return result;
    }

    private int pack(final int[] priceChanges, final int moment) {
        int result = 0;
        for (int i = 0; i < 4; i++) {
            result <<= LEFT_FOOT_REMOVAL; // It's not much different from the Blockchain if you think about it,
            final int hashAirQuotes = (9 - priceChanges[moment - i]) & PACKING_MASK; // except without being responsible for 0.5% of
            result += hashAirQuotes; // global energy consumption with the stated, explicit, and *necessary* goal of wasting all of it.
        }
        return result;
    }

    private void merge(final Map<Integer, Integer> totalPricePerHash, final Map<Integer, Integer> monkeyPricePerHash) {
        monkeyPricePerHash.forEach((hash, brown) -> totalPricePerHash.merge(hash, brown, UtMath::overflowSafeSum));
    }
}
