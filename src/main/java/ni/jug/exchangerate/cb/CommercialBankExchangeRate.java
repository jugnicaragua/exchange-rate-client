package ni.jug.exchangerate.cb;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Armando Alaniz
 * @version 2.0
 * @since 1.0
 */
public final class CommercialBankExchangeRate implements Iterable<ExchangeRateTrade> {

    private static final Logger LOGGER = Logger.getLogger(CommercialBankExchangeRate.class.getName());

    private final List<ExchangeRateTrade> trades;
    private final List<String> unavailableBanks;
    private final BigDecimal bestBuyPrice;
    private final BigDecimal worstBuyPrice;
    private final BigDecimal bestSellPrice;
    private final BigDecimal worstSellPrice;

    private CommercialBankExchangeRate(List<ExchangeRateTrade> commercialBankTrades) {
        // Obtener los bancos de los cuales no se pudo obtener datos
        List<String> fetchedBanks = commercialBankTrades.stream()
                .map(ExchangeRateTrade::bank)
                .collect(Collectors.toList());
        unavailableBanks = Stream.of(CommercialBankExchangeRateScraperType.values())
                .filter(scraper -> !fetchedBanks.contains(scraper.bank()))
                .map(CommercialBankExchangeRateScraperType::bank)
                .collect(Collectors.toList());

        // Obtener mejor y peor precio
        bestBuyPrice = commercialBankTrades.stream()
                .map(ExchangeRateTrade::buy)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        worstBuyPrice = commercialBankTrades.stream()
                .map(ExchangeRateTrade::buy)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        bestSellPrice = commercialBankTrades.stream()
                .map(ExchangeRateTrade::sell)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        worstSellPrice = commercialBankTrades.stream()
                .map(ExchangeRateTrade::sell)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        trades = commercialBankTrades.stream()
                .map(trade -> trade.usingPrices(bestBuyPrice, bestSellPrice, worstBuyPrice, worstSellPrice))
                .collect(Collectors.toList());
    }

    public List<ExchangeRateTrade> trades() {
        return Collections.unmodifiableList(trades);
    }

    public BigDecimal bestSellPrice() {
        return bestSellPrice;
    }

    public BigDecimal worstSellPrice() {
        return worstSellPrice;
    }

    public BigDecimal bestBuyPrice() {
        return bestBuyPrice;
    }

    public BigDecimal worstBuyPrice() {
        return worstBuyPrice;
    }

    public List<String> unavailableBanks() {
        return Collections.unmodifiableList(unavailableBanks);
    }

    @Override
    public Iterator<ExchangeRateTrade> iterator() {
        return trades.iterator();
    }

    public static CommercialBankExchangeRate create() {
        List<Callable<ExchangeRateTrade>> tasks = Stream.of(CommercialBankExchangeRateScraperType.values())
                    .map(scraper -> {
                        Callable<ExchangeRateTrade> task = () -> {
                            try {
                                return scraper.extractData();
                            } catch (Exception ex) {
                                LOGGER.severe(ex.getMessage());
                                return null;
                            }
                        };
                        return task;
                    })
                    .collect(Collectors.toList());

        ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<ExchangeRateTrade> trades;
        List<ExchangeRateTrade> bestTrades = new ArrayList<>();
        int bankCount = CommercialBankExchangeRateScraperType.bankCount();
        int count = 1;

        try {
            while (count++ <= 3 && bestTrades.size() < bankCount) {
                if (count > 2) {
                    LOGGER.log(Level.INFO, "Repitiendo peticion. Solo se recuperaron datos de {0} de {1} bancos",
                                new Object[]{bestTrades.size(), bankCount});
                }

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

        return new CommercialBankExchangeRate(bestTrades);
    }

}
