package ni.jug.cb.exchangerate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Armando Alaniz
 * @since 1.0
 * @version 1.0
 */
public final class ExchangeRateCBClient {

    private static final Logger LOGGER = Logger.getLogger(ExchangeRateCBClient.class.getName());

    private final List<ExchangeRateTrade> trades;
    private final List<String> unavailableBanks;
    private final BigDecimal bestBuyPrice;
    private final BigDecimal worstBuyPrice;
    private final BigDecimal bestSellPrice;
    private final BigDecimal worstSellPrice;

    public ExchangeRateCBClient() {
        trades = startCrawling();

        // Obtener los bancos de los cuales no se pudo obtener datos
        List<String> fetchedBanks = trades.stream()
                .map(ExchangeRateTrade::bank)
                .collect(Collectors.toList());
        unavailableBanks = Stream.of(ExchangeRateScraperType.values())
                .filter(scraper -> !fetchedBanks.contains(scraper.bank()))
                .map(ExchangeRateScraperType::bank)
                .collect(Collectors.toList());

        // Obtener mejor y peor precio
        bestBuyPrice = trades.stream()
                .map(ExchangeRateTrade::buy)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        worstBuyPrice = trades.stream()
                .map(ExchangeRateTrade::buy)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        bestSellPrice = trades.stream()
                .map(ExchangeRateTrade::sell)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        worstSellPrice = trades.stream()
                .map(ExchangeRateTrade::sell)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
    }

    private List<ExchangeRateTrade> startCrawling() {
        List<Callable<ExchangeRateTrade>> tasks = Stream.of(ExchangeRateScraperType.values())
                .map(bank -> {
                    Callable<ExchangeRateTrade> task = () -> {
                        try {
                            return bank.extractData();
                        } catch (Exception ex) {
                            LOGGER.severe(ex.getMessage());
                            return null;
                        }
                    };
                    return task;
                })
                .collect(Collectors.toList());

        ExecutorService service = Executors.newFixedThreadPool(4);

        try {
            List<Future<ExchangeRateTrade>> futures = service.invokeAll(tasks);
            return futures.stream()
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
        } catch (InterruptedException ie) {
            LOGGER.severe(ie.getMessage());
        } finally {
            service.shutdown();
        }

        return Collections.emptyList();
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

    boolean repeatRequest() {
        return unavailableBanks.size() > 0;
    }

    public int fetchedBankCount() {
        return trades.size();
    }

    public List<String> unavailableBanks() {
        return Collections.unmodifiableList(unavailableBanks);
    }

    public static ExchangeRateCBClient scrapAndRepeatIfNecessary() {
        int count = 1;
        ExchangeRateCBClient client = new ExchangeRateCBClient();

        while (client.repeatRequest() && count++ <= 3) {
            LOGGER.log(Level.INFO, "Repitiendo peticion. Solo se recuperaron datos de {0} bancos de un total de {1}",
                    new Object[]{client.fetchedBankCount(), ExchangeRateScraperType.bankCount()});
            client = new ExchangeRateCBClient();
        }

        return client;
    }

}
