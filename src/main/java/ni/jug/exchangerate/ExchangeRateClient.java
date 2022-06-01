package ni.jug.exchangerate;

import ni.jug.exchangerate.bank.BankQuery;
import ni.jug.exchangerate.centralbank.CentralBankQuery;
import ni.jug.exchangerate.centralbank.CentralBankScraper;
import ni.jug.exchangerate.centralbank.CentralBankScraperRetryDecorator;

/**
 * @author aalaniz
 */
public final class ExchangeRateClient {

    public static final ExchangeRateClient INSTANCE = new ExchangeRateClient();

    private ExchangeRateClient() {
    }

    public CentralBankQuery centralBankQuery() {
        return new CentralBankScraperRetryDecorator(CentralBankScraper.INSTANCE);
    }

    public CentralBankQuery centralBankQuery(int retry) {
        return new CentralBankScraperRetryDecorator(CentralBankScraper.INSTANCE, retry);
    }

    public BankQuery bankQuery() {
        return BankQuery.INSTANCE;
    }
}
