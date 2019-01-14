package ni.jug.cb.exchangerate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Armando Alaniz
 * @since 1.0
 * @version 1.0
 */
public class ExchangeRateClient {

    private List<ExchangeRateTrade> trades;
    private List<String> unavailableBanks;
    private BigDecimal bestBuyPrice;
    private BigDecimal worstBuyPrice;
    private BigDecimal bestSellPrice;
    private BigDecimal worstSellPrice;
    private List<String> bestSellingOption;

    public ExchangeRateClient() {
        List<Callable<ExchangeRateTrade>> tasks = Stream.of(ExchangeRateScraperType.values())
                .map(bank -> {
                    Callable<ExchangeRateTrade> task = () -> {
                        return bank.extractData();
                    };
                    return task;
                })
                .collect(Collectors.toList());

        ExecutorService service = Executors.newFixedThreadPool(4);
        try {
            List<Future<ExchangeRateTrade>> futures = service.invokeAll(tasks);
            trades = futures
                .stream()
                .map(f -> {
                    try {
                        return f.get();
                    } catch (InterruptedException | ExecutionException ex) {
                        // TODO Log the error
                        System.out.println("--> " + ex.getMessage());
                        return null;
                    }
                })
                .filter(data -> data != null)
                .collect(Collectors.toList());

            trades.forEach((trade) -> System.out.println("--> " + trade));

            // Calcular los bancos de los cuales no se pudo obtener datos
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
                    .map(ExchangeRateTrade::getBuy)
                    .max(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);

            // Lista de bancos con las mejores y peores opciones
            bestSellingOption = trades.stream()
                    .filter(trade -> trade.getSell().compareTo(bestSellPrice) == 0)
                    .map(ExchangeRateTrade::getBank)
                    .collect(Collectors.toList());
        } catch (InterruptedException ie) {
            System.out.println("--> " + ie.getMessage());
        } finally {
            service.shutdown();
        }
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
        return Collections.emptyList();
    }

    public List<String> bestBuyingOption() {
        return Collections.emptyList();
    }

    public List<String> worstBuyingOption() {
        return Collections.emptyList();
    }

    public List<String> unavailableBanks() {
        return Collections.unmodifiableList(unavailableBanks);
    }

    public static void main(String[] args) {
        ExchangeRateClient client = new ExchangeRateClient();
        System.out.println("");
        System.out.println("------------------------------------------------------------------------");
        System.out.println("Bancos no disponibles");
        System.out.println("------------------------------------------------------------------------");
        System.out.println("--> " + client.unavailableBanks() + "\n");
        System.out.println("------------------------------------------------------------------------");
        System.out.println("Compra");
        System.out.println("------------------------------------------------------------------------");
        System.out.println("--> Mejor: " + client.bestBuyPrice + ". Peor: " + client.worstBuyPrice + "\n");
        System.out.println("------------------------------------------------------------------------");
        System.out.println("Venta");
        System.out.println("------------------------------------------------------------------------");
        System.out.println("--> Mejor: " + client.bestSellPrice + ". Peor: " + client.worstSellPrice + "\n");
    }

}
