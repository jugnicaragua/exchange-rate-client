package ni.jug.exchangerate;

import ni.jug.exchangerate.cb.CommercialBank;
import ni.jug.exchangerate.cb.CommercialBankExchangeRate;
import ni.jug.exchangerate.cb.CommercialBankExchangeRateScraperType;
import ni.jug.exchangerate.cb.ExchangeRateTrade;
import ni.jug.exchangerate.ncb.CentralBankExchangeRateScraper;
import ni.jug.exchangerate.ncb.MonthlyExchangeRate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 *
 * @author Armando Alaniz
 * @version 4.0
 * @since 2.0
 */
public final class ExchangeRateClient {

    public static BigDecimal getOfficialExchangeRate(LocalDate date) {
        return CentralBankExchangeRateScraper.getExchangeRateByDate(date);
    }

    public static MonthlyExchangeRate getOfficialMonthlyExchangeRate(YearMonth yearMonth) {
        return CentralBankExchangeRateScraper.getMonthlyExchangeRate(yearMonth);
    }

    public static MonthlyExchangeRate getOfficialMonthlyExchangeRate(LocalDate date) {
        return CentralBankExchangeRateScraper.getMonthlyExchangeRate(date);
    }

    public static CommercialBankExchangeRate commercialBankExchangeRate() {
        return CommercialBankExchangeRate.create();
    }

    public static List<ExchangeRateTrade> commercialBankTrades() {
        return CommercialBankExchangeRate.create().trades();
    }

    public static List<ExchangeRateTrade> commercialBankTrades(List<ExchangeRateTrade> trades) {
        return new CommercialBankExchangeRate(trades).trades();
    }

    public static List<CommercialBank> commercialBanks() {
        return CommercialBankExchangeRateScraperType.commercialBanks();
    }
}
