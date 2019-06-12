package ni.jug.exchangerate;

import ni.jug.exchangerate.cb.CommercialBankExchangeRate;
import ni.jug.exchangerate.cb.ExchangeRateTrade;
import ni.jug.exchangerate.ncb.MonthlyExchangeRate;
import ni.jug.exchangerate.ncb.NiCentralBankExchangeRateClient;
import ni.jug.exchangerate.ncb.NiCentralBankExchangeRateScraper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

/**
 *
 * @author Armando Alaniz
 * @version 1.0
 * @since 2.0
 */
public final class ExchangeRateClient {

    private final NiCentralBankExchangeRateClient niCentralBankExchangeRateClient;

    public ExchangeRateClient() {
        this.niCentralBankExchangeRateClient = new NiCentralBankExchangeRateScraper();
    }

    public BigDecimal getNiCentralBankExchangeRate(LocalDate date) {
        return niCentralBankExchangeRateClient.getExchangeRate(date);
    }

    public BigDecimal getNiCentralBankCurrentExchangeRate() {
        return niCentralBankExchangeRateClient.getCurrentExchangeRate();
    }

    public MonthlyExchangeRate getNiCentralBankMonthlyExchangeRate(int year, Month month) {
        return niCentralBankExchangeRateClient.getMonthlyExchangeRate(year, month);
    }

    public MonthlyExchangeRate getNiCentralBankMonthlyExchangeRate(int year, int month) {
        return niCentralBankExchangeRateClient.getMonthlyExchangeRate(year, month);
    }

    public MonthlyExchangeRate getNiCentralBankMonthlyExchangeRate(LocalDate date) {
        return niCentralBankExchangeRateClient.getMonthlyExchangeRate(date);
    }

    public MonthlyExchangeRate getNiCentralBankCurrentMonthExchangeRate() {
        return niCentralBankExchangeRateClient.getCurrentMonthExchangeRate();
    }

    public CommercialBankExchangeRate commercialBankExchangeRate() {
        return CommercialBankExchangeRate.create();
    }

    public List<ExchangeRateTrade> commercialBanktrades() {
        return CommercialBankExchangeRate.create().trades();
    }

}
