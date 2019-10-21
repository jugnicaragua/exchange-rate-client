package ni.jug.exchangerate;

import ni.jug.exchangerate.cb.CommercialBank;
import ni.jug.exchangerate.cb.CommercialBankExchangeRate;
import ni.jug.exchangerate.cb.CommercialBankExchangeRateScraperType;
import ni.jug.exchangerate.cb.ExchangeRateTrade;
import ni.jug.exchangerate.ncb.MonthlyExchangeRate;
import ni.jug.exchangerate.ncb.NiCentralBankExchangeRateClient;
import ni.jug.exchangerate.ncb.NiCentralBankExchangeRateScraper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 *
 * @author Armando Alaniz
 * @version 3.0
 * @since 2.0
 */
public final class ExchangeRateClient {

    private final NiCentralBankExchangeRateClient niCentralBankExchangeRateClient;

    public ExchangeRateClient() {
        this.niCentralBankExchangeRateClient = new NiCentralBankExchangeRateScraper();
    }

    public BigDecimal getOfficialExchangeRate(LocalDate date) {
        return niCentralBankExchangeRateClient.getExchangeRate(date);
    }

    public BigDecimal getOfficialCurrentExchangeRate() {
        return niCentralBankExchangeRateClient.getCurrentExchangeRate();
    }

    public MonthlyExchangeRate getOfficialMonthlyExchangeRate(YearMonth yearMonth) {
        return niCentralBankExchangeRateClient.getMonthlyExchangeRate(yearMonth);
    }

    public MonthlyExchangeRate getOfficialMonthlyExchangeRate(LocalDate date) {
        return niCentralBankExchangeRateClient.getMonthlyExchangeRate(date);
    }

    public MonthlyExchangeRate getOfficialCurrentMonthlyExchangeRate() {
        return niCentralBankExchangeRateClient.getCurrentMonthlyExchangeRate();
    }

    public CommercialBankExchangeRate commercialBankExchangeRate() {
        return CommercialBankExchangeRate.create();
    }

    public List<ExchangeRateTrade> commercialBankTrades() {
        return CommercialBankExchangeRate.create().trades();
    }

    public List<ExchangeRateTrade> commercialBankTrades(List<ExchangeRateTrade> trades) {
        return new CommercialBankExchangeRate(trades).trades();
    }

    public static List<CommercialBank> commercialBanks() {
        return CommercialBankExchangeRateScraperType.commercialBanks();
    }

}
