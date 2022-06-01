package ni.jug.exchangerate.bank;

import java.util.logging.Logger;

/**
 * @author aalaniz
 */
public class BankScraperExecutorRetryDecorator extends BankScraperExecutor {

    private static final Logger LOGGER = Logger.getLogger(BankScraperExecutorRetryDecorator.class.getName());

    public static final BankScraperExecutorRetryDecorator INSTANCE = new BankScraperExecutorRetryDecorator(BankScraperExecutor.INSTANCE);

    private final BankScraperExecutor delegator;

    private BankScraperExecutorRetryDecorator(BankScraperExecutor delegator) {
        this.delegator = delegator;
    }

    @Override
    public BankExchangeRateSummary execute() {
        BankExchangeRateSummary bankExchangeRateSummary = delegator.execute();

        if (bankExchangeRateSummary.unavailableBanks().size() >= 2) {
            LOGGER.severe("Peticion #2: no se pudo extraer la info de al menos 2 bancos");

            bankExchangeRateSummary = delegator.execute();
        }

        return bankExchangeRateSummary;
    }
}
