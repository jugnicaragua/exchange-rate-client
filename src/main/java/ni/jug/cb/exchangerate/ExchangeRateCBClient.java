package ni.jug.cb.exchangerate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Armando Alaniz
 * @since 1.0
 * @version 1.0
 */
public class ExchangeRateCBClient {

    private static final Logger LOGGER = Logger.getLogger(ExchangeRateCBClient.class.getName());

    private final List<ExchangeRateTrade> trades;
    private final List<String> unavailableBanks;
    private final BigDecimal bestBuyPrice;
    private final BigDecimal worstBuyPrice;
    private final BigDecimal bestSellPrice;
    private final BigDecimal worstSellPrice;
    private final List<String> bestSellingOption;
    private final List<String> worstSellingOption;
    private final List<String> bestBuyingOption;
    private final List<String> worstBuyingOption;

    public ExchangeRateCBClient() {
        trades = startCrawling();

        // Obtener los bancos de los cuales no se pudo obtener datos
        List<String> banks = trades.stream()
                .map(ExchangeRateTrade::getBank)
                .collect(Collectors.toList());
        unavailableBanks = Stream.of(ExchangeRateScraperType.values())
                .filter(scraper -> !banks.contains(scraper.bank()))
                .map(ExchangeRateScraperType::bank)
                .collect(Collectors.toList());

        // Obtener mejor y peor precio
        bestBuyPrice = trades.stream()
                .map(ExchangeRateTrade::getBuy)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        worstBuyPrice = trades.stream()
                .map(ExchangeRateTrade::getBuy)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        bestSellPrice = trades.stream()
                .map(ExchangeRateTrade::getSell)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        worstSellPrice = trades.stream()
                .map(ExchangeRateTrade::getSell)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        // Lista de bancos con las mejores y peores opciones
        bestSellingOption = trades.stream()
                .filter(trade -> trade.isSellEqual(bestSellPrice))
                .map(ExchangeRateTrade::getBank)
                .collect(Collectors.toList());

        worstSellingOption = trades.stream()
                .filter(trade -> trade.isSellEqual(worstSellPrice))
                .map(ExchangeRateTrade::getBank)
                .collect(Collectors.toList());

        bestBuyingOption = trades.stream()
                .filter(trade -> trade.isBuyEqual(bestBuyPrice))
                .map(ExchangeRateTrade::getBank)
                .collect(Collectors.toList());

        worstBuyingOption = trades.stream()
                .filter(trade -> trade.isBuyEqual(worstBuyPrice))
                .map(ExchangeRateTrade::getBank)
                .collect(Collectors.toList());
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

    public List<ExchangeRateTrade> getTrades() {
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

    public List<String> bestSellingOption() {
        return Collections.unmodifiableList(bestSellingOption);
    }

    public List<String> worstSellingOption() {
        return Collections.unmodifiableList(worstSellingOption);
    }

    public List<String> bestBuyingOption() {
        return Collections.unmodifiableList(bestBuyingOption);
    }

    public List<String> worstBuyingOption() {
        return Collections.unmodifiableList(worstBuyingOption);
    }

    public List<String> unavailableBanks() {
        return Collections.unmodifiableList(unavailableBanks);
    }

}
