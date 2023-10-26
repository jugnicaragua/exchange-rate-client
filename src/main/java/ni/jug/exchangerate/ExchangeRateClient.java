package ni.jug.exchangerate;

import ni.jug.exchangerate.bank.BankQuery;
import ni.jug.exchangerate.centralbank.CentralBankQuery;
import ni.jug.exchangerate.centralbank.CentralBankScraper;
import ni.jug.exchangerate.centralbank.CentralBankScraperRetryDecorator;

/**
 * @author aalaniz
 */
public final class ExchangeRateClient { //Comentando mi gente

    public static final ExchangeRateClient INSTANCE = new ExchangeRateClient();

    /**
     * This is a default public constructor.
     */
    private ExchangeRateClient() {
    }

    public CentralBankQuery centralBankQuery() {
        return new CentralBankScraperRetryDecorator(CentralBankScraper.INSTANCE);
    }

    // @vato_dev

    public CentralBankQuery centralBankQuery(int retry) {
        return new CentralBankScraperRetryDecorator(CentralBankScraper.INSTANCE, retry);
    }

    public BankQuery bankQuery() {
        return BankQuery.INSTANCE;
    }
}
