package ni.jug.exchangerate.centralbank;

import ni.jug.exchangerate.ExchangeRateException;
import ni.jug.exchangerate.RetryExchangeRateException;
import ni.jug.util.Inputs;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author aalaniz
 */
public class CentralBankScraperRetryDecorator implements CentralBankQuery {

    private static final Logger LOGGER = Logger.getLogger(CentralBankScraperRetryDecorator.class.getName());

    private final CentralBankQuery delegator;
    private final int retry;

    public CentralBankScraperRetryDecorator(CentralBankQuery centralBankQuery) {
        this.delegator = centralBankQuery;
        this.retry = 3;
    }

    public CentralBankScraperRetryDecorator(CentralBankQuery centralBankQuery, int retry) {
        this.delegator = centralBankQuery;
        this.retry = Inputs.numberInRange(retry, 1, 10);
    }

    @Override
    public BigDecimal getExchangeRate(LocalDate date) throws ExchangeRateException {
        return retry(date, delegator::getExchangeRate, BigDecimal.ZERO);
    }

    @Override
    public MonthlyExchangeRate getMonthlyExchangeRate(YearMonth period) throws ExchangeRateException {
        return retry(period, delegator::getMonthlyExchangeRate, MonthlyExchangeRate.create());
    }

    private <I, O> O retry(I input, CentralBankFunctionWrapper<I, O> scraper, O defaultValue) throws ExchangeRateException {
        ExchangeRateException lastError = null;

        for (int i = 1; i <= retry; i++) {
            LOGGER.log(Level.INFO, "BCN: peticion #{0}", i);

            O response = null;
            try {
                response = scraper.apply(input);
            } catch (RetryExchangeRateException exception) {
                LOGGER.log(Level.SEVERE, "No se pudo obtener el tipo de cambio del BCN", exception);
                lastError = exception;
            }

            if (response != null) {
                return response;
            }
        }

        if (lastError != null) {
            throw lastError;
        }

        return defaultValue;
    }
}
