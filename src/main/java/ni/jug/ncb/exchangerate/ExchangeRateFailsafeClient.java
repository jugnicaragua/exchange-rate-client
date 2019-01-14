package ni.jug.ncb.exchangerate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.logging.Logger;
import javax.xml.ws.WebServiceException;

/**
 *
 * @author Armando Alaniz
 * @version 1.0
 * @since 2.0
 */
public class ExchangeRateFailsafeClient implements ExchangeRateClient {

    private static final Logger LOGGER = Logger.getLogger(ExchangeRateFailsafeClient.class.getName());

    private final ExchangeRateClient wsClient;
    private final ExchangeRateClient scraperClient;

    public ExchangeRateFailsafeClient() {
        this.wsClient = new ExchangeRateWSClient();
        this.scraperClient = new ExchangeRateScraper();
    }

    @Override
    public BigDecimal getExchangeRate(LocalDate date) {
        try {
            BigDecimal exchangeRate = wsClient.getExchangeRate(date);
            if (exchangeRate.compareTo(BigDecimal.ZERO) == 0) {
                return scraperClient.getExchangeRate(date);
            } else {
                return exchangeRate;
            }
        } catch (WebServiceException wse) {
            LOGGER.severe(wse.getMessage());
            return scraperClient.getExchangeRate(date);
        }
    }

    @Override
    public MonthlyExchangeRate getMonthlyExchangeRate(int year, Month month) {
        try {
            MonthlyExchangeRate monthlyExchangeRate = wsClient.getMonthlyExchangeRate(year, month);
            if (monthlyExchangeRate.isEmpty()) {
                return scraperClient.getMonthlyExchangeRate(year, month);
            } else {
                return monthlyExchangeRate;
            }
        } catch (WebServiceException wse) {
            LOGGER.severe(wse.getMessage());
            return scraperClient.getMonthlyExchangeRate(year, month);
        }
    }

}
