package ni.jug.exchangerate.bank;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author aalaniz
 */
public final class BankExchangeRateSummary implements Iterable<ExchangeRateTrade> {

    private final List<ExchangeRateTrade> trades;
    private final List<String> unavailableBanks;
    private final ExchangeRateStatistics statistics;

    public BankExchangeRateSummary(List<ExchangeRateTrade> bankTrades) {
        Objects.requireNonNull(bankTrades);
        statistics = bankTrades.stream()
                .collect(ExchangeRateStatistics::new, ExchangeRateStatistics::accumulate, ExchangeRateStatistics::combine);

        unavailableBanks = Stream.of(BankScraper.values())
                .map(BankScraper::bank)
                .filter(bank -> !statistics.banks.contains(bank))
                .collect(Collectors.toList());

        trades = bankTrades.stream()
                .map(trade -> trade.withBestPrices(statistics.bestBuyPrice, statistics.bestSellPrice, statistics.worstBuyPrice,
                        statistics.worstSellPrice))
                .collect(Collectors.toList());
    }

    public List<ExchangeRateTrade> trades() {
        return Collections.unmodifiableList(trades);
    }

    public BigDecimal bestSellPrice() {
        return statistics.bestSellPrice;
    }

    public BigDecimal worstSellPrice() {
        return statistics.worstSellPrice;
    }

    public BigDecimal bestBuyPrice() {
        return statistics.bestBuyPrice;
    }

    public BigDecimal worstBuyPrice() {
        return statistics.worstBuyPrice;
    }

    public List<String> unavailableBanks() {
        return Collections.unmodifiableList(unavailableBanks);
    }

    @Override
    public Iterator<ExchangeRateTrade> iterator() {
        return trades.iterator();
    }

    class ExchangeRateStatistics {
        Set<String> banks;
        BigDecimal bestSellPrice;
        BigDecimal worstSellPrice;
        BigDecimal bestBuyPrice;
        BigDecimal worstBuyPrice;

        ExchangeRateStatistics() {
            banks = new HashSet<>();
        }

        public void accumulate(ExchangeRateTrade trade) {
            banks.add(trade.bank());
            if (bestSellPrice == null || trade.sell().compareTo(bestSellPrice) < 0) {
                bestSellPrice = trade.sell();
            }
            if (worstSellPrice == null || trade.sell().compareTo(worstSellPrice) > 0) {
                worstSellPrice = trade.sell();
            }
            if (bestBuyPrice == null || trade.buy().compareTo(bestBuyPrice) > 0) {
                bestBuyPrice = trade.buy();
            }
            if (worstBuyPrice == null || trade.buy().compareTo(worstBuyPrice) < 0) {
                worstBuyPrice = trade.buy();
            }
        }

        public void combine(ExchangeRateStatistics other) {
            banks.addAll(other.banks);
            if (bestSellPrice == null || other.bestSellPrice.compareTo(bestSellPrice) < 0) {
                bestSellPrice = other.bestSellPrice;
            }
            if (worstSellPrice == null || other.worstSellPrice.compareTo(worstSellPrice) > 0) {
                worstSellPrice = other.worstSellPrice;
            }
            if (bestBuyPrice == null || other.bestBuyPrice.compareTo(bestBuyPrice) > 0) {
                bestBuyPrice = other.bestBuyPrice;
            }
            if (worstBuyPrice == null || other.worstBuyPrice.compareTo(worstBuyPrice) < 0) {
                worstBuyPrice = other.worstBuyPrice;
            }
        }
    }
}
