package ni.jug.exchangerate;

import ni.jug.exchangerate.bank.Bank;
import ni.jug.exchangerate.bank.BankExchangeRateSummary;
import ni.jug.exchangerate.bank.BankScraper;
import ni.jug.exchangerate.bank.ExchangeRateTrade;
import ni.jug.exchangerate.centralbank.MonthlyExchangeRate;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author aalaniz
 */
public class ExchangeRateTest {

    @Test
    void testExchangeRateAtSpecificDate() throws ExchangeRateException {
        assertEquals(new BigDecimal("31.9396"), ExchangeRateClient.INSTANCE
                .centralBankQuery()
                .getExchangeRate(LocalDate.of(2018, 10, 1)));
        assertEquals(new BigDecimal("32.0679"), ExchangeRateClient.INSTANCE
                .centralBankQuery()
                .getExchangeRate(LocalDate.of(2018, 10, 31)));
    }

    @Test
    void testMonthlyExchangeRateAtSpecificDate() throws ExchangeRateException {
        MonthlyExchangeRate monthlyExchangeRate = ExchangeRateClient.INSTANCE
                .centralBankQuery()
                .getMonthlyExchangeRate(YearMonth.of(2018, 10));

        assertEquals(31, monthlyExchangeRate.size());
        assertEquals(new BigDecimal("31.9396"), monthlyExchangeRate.getFirstExchangeRate());
        assertEquals(new BigDecimal("32.0679"), monthlyExchangeRate.getLastExchangeRate());
        assertEquals(new BigDecimal("31.9994"), monthlyExchangeRate.getExchangeRate(LocalDate.of(2018, 10, 15)));
        assertEquals(BigDecimal.ZERO, monthlyExchangeRate.getExchangeRate(LocalDate.of(2018, 9, 30)));
        assertFalse(monthlyExchangeRate.isHasGaps());

        LocalDate date1 = LocalDate.of(2018, 10, 1);
        LocalDate date2 = LocalDate.of(2018, 10, 15);
        Map<LocalDate, BigDecimal> rangeOfValues = monthlyExchangeRate.getExchangeRateBetween(date1, date2);
        assertEquals(15, rangeOfValues.size());
        assertEquals(new BigDecimal("31.9396"), rangeOfValues.get(date1));
        assertEquals(new BigDecimal("31.9994"), rangeOfValues.get(date2));
    }

    @Test
    void testExchangeRateAtSpecificDateUsingRetry() throws ExchangeRateException {
        assertEquals(new BigDecimal("31.9396"), ExchangeRateClient.INSTANCE
                .centralBankQuery(5)
                .getExchangeRate(LocalDate.of(2018, 10, 1)));
        assertEquals(new BigDecimal("32.0679"), ExchangeRateClient.INSTANCE
                .centralBankQuery(5)
                .getExchangeRate(LocalDate.of(2018, 10, 31)));
    }

    @Test
    void testValidationOfYear() {
        assertThrows(ExchangeRateException.class, () -> {
            ExchangeRateClient.INSTANCE
                    .centralBankQuery()
                    .getExchangeRate(LocalDate.of(2011, 12, 31));
        });
        assertThrows(ExchangeRateException.class, () -> {
            ExchangeRateClient.INSTANCE
                    .centralBankQuery()
                    .getMonthlyExchangeRate(YearMonth.of(2011, Month.DECEMBER));
        });
        assertThrows(ExchangeRateException.class, () -> {
            ExchangeRateClient.INSTANCE
                    .centralBankQuery()
                    .getMonthlyExchangeRate(YearMonth.of(2011, 10));
        });
    }

    @Test
    void testBANPRO() {
        assertDoesNotThrow(() -> {
            BankScraper scraper = BankScraper.BANPRO;
            scraper.fetchData();
        });
    }

    @Test
    void testFICOHSA() {
        assertDoesNotThrow(() -> {
            BankScraper scraper = BankScraper.FICOHSA;
            scraper.fetchData();
        });
    }

    @Test
    void testAVANZ() {
        assertDoesNotThrow(() -> {
            BankScraper scraper = BankScraper.AVANZ;
            scraper.fetchData();
        });
    }

    @Test
    void testBAC() {
        assertDoesNotThrow(() -> {
            BankScraper scraper = BankScraper.BAC;
            scraper.fetchData();
        });
    }

    @Test
    void testBDF() {
        assertDoesNotThrow(() -> {
            BankScraper scraper = BankScraper.BDF;
            scraper.fetchData();
        });
    }

    @Test
    void testLAFISE() {
        assertDoesNotThrow(() -> {
            BankScraper scraper = BankScraper.LAFISE;
            scraper.fetchData();
        });
    }

    @Test
    void testBanks() {
        List<Bank> banks = ExchangeRateClient.INSTANCE.bankQuery().banks();

        assertEquals(BankScraper.count(), banks.size());
    }

    @Test
    void testBankSummary() {
        BankExchangeRateSummary bankExchangeRateSummary = ExchangeRateClient.INSTANCE.bankQuery().bankSummary();
        assertFalse(bankExchangeRateSummary.trades().isEmpty());
    }

    @Test
    void testBankTrades() {
        List<ExchangeRateTrade> bankTrades = ExchangeRateClient.INSTANCE.bankQuery().bankTrades();
        assertFalse(bankTrades.isEmpty());
    }
}
