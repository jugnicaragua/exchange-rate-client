package ni.jug.exchangerate.bank;

import java.util.List;

/**
 * @author aalaniz
 */
public class BankQuery {

    public static final BankQuery INSTANCE = new BankQuery();

    private BankQuery() {
    }

    public BankExchangeRateSummary bankSummary() {
        return BankScraperExecutorRetryDecorator.INSTANCE.execute();
    }

    public List<ExchangeRateTrade> bankTrades() {
        return BankScraperExecutorRetryDecorator.INSTANCE.execute().trades();
    }

    public List<ExchangeRateTrade> recalculateTrades(List<ExchangeRateTrade> trades) {
        return new BankExchangeRateSummary(trades).trades();
    }

    public List<Bank> banks() {
        return BankScraper.banks();
    }
}
