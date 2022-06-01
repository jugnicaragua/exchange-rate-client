package ni.jug.exchangerate.bank;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class BankScraperExecutor {

    private static final Logger LOGGER = Logger.getLogger(BankScraperExecutor.class.getName());

    public static final BankScraperExecutor INSTANCE = new BankScraperExecutor();

    protected BankScraperExecutor() {
    }

    public BankExchangeRateSummary execute() {
        ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        List<Callable<ExchangeRateTrade>> tasks = BankScraper.createAsyncTasks();
        List<ExchangeRateTrade> trades = Collections.emptyList();

        try {
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
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (InterruptedException exception) {
            LOGGER.log(Level.SEVERE, "Error getting exchange rates from banks", exception);
        } finally {
            service.shutdown();
        }

        return new BankExchangeRateSummary(trades);
    }
}
