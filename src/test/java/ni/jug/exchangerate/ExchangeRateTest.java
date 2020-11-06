package ni.jug.exchangerate;

import ni.jug.exchangerate.cb.CommercialBankExchangeRateScraperType;
import ni.jug.exchangerate.ncb.MonthlyExchangeRate;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author Armando Alaniz
 * @version 2.0
 * @since 2.0
 */
public class ExchangeRateTest {

    @Test
    public void testExchangeRateAtSpecificDate() throws ExchangeRateException {
        assertEquals(new BigDecimal("31.9396"), ExchangeRateClient.getOfficialExchangeRate(LocalDate.of(2018, 10, 1)));
        assertEquals(new BigDecimal("32.0679"), ExchangeRateClient.getOfficialExchangeRate(LocalDate.of(2018, 10, 31)));
    }

    @Test
    public void testMonthlyExchangeRateAtSpecificDate() throws ExchangeRateException {
        MonthlyExchangeRate monthlyExchangeRate = ExchangeRateClient.getOfficialMonthlyExchangeRate(YearMonth.of(2018, 10));

        assertEquals(31, monthlyExchangeRate.size());
        assertEquals(new BigDecimal("31.9396"), monthlyExchangeRate.getFirstExchangeRate());
        assertEquals(new BigDecimal("32.0679"), monthlyExchangeRate.getLastExchangeRate());
        assertEquals(new BigDecimal("31.9994"), monthlyExchangeRate.getExchangeRate(LocalDate.of(2018, 10, 15)));
        assertEquals(BigDecimal.ZERO, monthlyExchangeRate.getExchangeRate(LocalDate.of(2018, 9, 30)));
        assertFalse(monthlyExchangeRate.isIncomplete());

        LocalDate date1 = LocalDate.of(2018, 10, 1);
        LocalDate date2 = LocalDate.of(2018, 10, 15);
        Map<LocalDate, BigDecimal> rangeOfValues = monthlyExchangeRate.getExchangeRateBetween(date1, date2);
        assertEquals(15, rangeOfValues.size());
        assertEquals(new BigDecimal("31.9396"), rangeOfValues.get(date1));
        assertEquals(new BigDecimal("31.9994"), rangeOfValues.get(date2));
    }

    @Test
    public void testValidationOfYear() {
        assertThrows(IllegalArgumentException.class, () -> {
            ExchangeRateClient.getOfficialExchangeRate(LocalDate.of(2011, 12, 31));
        });
        assertThrows(IllegalArgumentException.class, () -> {
            ExchangeRateClient.getOfficialMonthlyExchangeRate(YearMonth.of(2011, Month.DECEMBER));
        });
        assertThrows(IllegalArgumentException.class, () -> {
            ExchangeRateClient.getOfficialMonthlyExchangeRate(YearMonth.of(2011, 10));
        });
        assertThrows(IllegalArgumentException.class, () -> {
            ExchangeRateClient.getOfficialMonthlyExchangeRate(LocalDate.of(2011, 12, 1));
        });
    }

    @Test
    public void testLAFISE() {
        CommercialBankExchangeRateScraperType scraper = CommercialBankExchangeRateScraperType.LAFISE;
        scraper.extractData();
    }

    @Test
    public void testBAC() {
        CommercialBankExchangeRateScraperType scraper = CommercialBankExchangeRateScraperType.BAC;
        scraper.extractData();
    }
}
