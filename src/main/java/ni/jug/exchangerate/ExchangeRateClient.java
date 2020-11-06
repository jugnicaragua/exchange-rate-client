package ni.jug.exchangerate;

import ni.jug.exchangerate.cb.CommercialBank;
import ni.jug.exchangerate.cb.CommercialBankRequestor;
import ni.jug.exchangerate.cb.CommercialBankScraperType;
import ni.jug.exchangerate.cb.ExchangeRateTrade;
import ni.jug.exchangerate.ncb.CentralBankScraper;
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
    public static final int MAX_RETRY_COUNT = 3;

    public static BigDecimal getOfficialExchangeRate(LocalDate date) throws ExchangeRateException {
        MonthlyExchangeRate monthlyExchangeRate = getOfficialMonthlyExchangeRate(date);
        return monthlyExchangeRate.getExchangeRate(date);
    }

    public static MonthlyExchangeRate getOfficialMonthlyExchangeRate(LocalDate date) throws ExchangeRateException {
        return getOfficialMonthlyExchangeRate(YearMonth.from(date));
    }

    public static MonthlyExchangeRate getOfficialMonthlyExchangeRate(YearMonth yearMonth) throws ExchangeRateException {
        return getOfficialMonthlyExchangeRate(yearMonth, MAX_RETRY_COUNT);
    }

    public static MonthlyExchangeRate getOfficialMonthlyExchangeRate(YearMonth yearMonth, int retryMaxCount) throws ExchangeRateException {
        return CentralBankScraper.getMonthlyExchangeRate(yearMonth, retryMaxCount);
    }

    public static CommercialBankRequestor commercialBankRequestor() {
        return CommercialBankRequestor.create();
    }

    public static List<ExchangeRateTrade> commercialBankTrades() {
        return CommercialBankRequestor.create().trades();
    }

    public static List<ExchangeRateTrade> commercialBankTrades(List<ExchangeRateTrade> trades) {
        return new CommercialBankRequestor(trades).trades();
    }

    public static List<CommercialBank> commercialBanks() {
        return CommercialBankScraperType.commercialBanks();
    }
}
