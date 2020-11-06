package ni.jug.exchangerate.cb;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Armando Alaniz
 * @version 3.0
 * @since 1.0
 */
public final class CommercialBankRequestor implements Iterable<ExchangeRateTrade> {

    private static final Logger LOGGER = Logger.getLogger(CommercialBankRequestor.class.getName());

    private final List<ExchangeRateTrade> trades;
    private final List<String> unavailableBanks;
    private final ExchangeRateStatistics statistics;

    public CommercialBankRequestor(List<ExchangeRateTrade> commercialBankTrades) {
        statistics = commercialBankTrades.stream()
                    .collect(ExchangeRateStatistics::new, ExchangeRateStatistics::accumulate, ExchangeRateStatistics::combine);

        unavailableBanks = Stream.of(CommercialBankScraper.values())
                    .map(CommercialBankScraper::bank)
                    .filter(bank -> !statistics.banks.contains(bank))
                    .collect(Collectors.toList());

        trades = commercialBankTrades.stream()
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

    public static CommercialBankRequestor create() {
        ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<ExchangeRateTrade> trades;
        List<ExchangeRateTrade> bestTrades = Collections.emptyList();
        int bankCount = CommercialBankScraper.bankCount();
        int count = 1;

        try {
            while (count++ <= 3 && bestTrades.size() < bankCount) {
                if (count > 2) {
                    LOGGER.log(Level.INFO, "Repitiendo peticion: {0} de {1} bancos fueron recuperados con exito",
                                new Object[] {bestTrades.size(), bankCount});
                }

                List<Callable<ExchangeRateTrade>> tasks = CommercialBankScraper.createTasks();
                List<Future<ExchangeRateTrade>> futures = service.invokeAll(tasks);
                trades = futures.stream()
                            .map(f -> {
                                try {
                                    return f.get();
                                } catch (InterruptedException | ExecutionException ex) {
                                    LOGGER.severe(ex.getMessage());
                                    return null;
                                }
                            })
                            .filter(data -> data != null)
                            .collect(Collectors.toList());

                if (trades.size() > bestTrades.size()) {
                    bestTrades = trades;
                }
            }
        } catch (InterruptedException ie) {
            LOGGER.severe(ie.getMessage());
        } finally {
            service.shutdown();
        }

        return new CommercialBankRequestor(bestTrades);
    }
}
