package ni.jug.ncb.exchangerate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Armando Alaniz
 * @version 2.0
 * @since 1.0
 */
public class ExchangeRateWSClientTest {

    private ExchangeRateClient getClient() {
        return new ExchangeRateWSClient();
    }

    @Test
    public void testExchangeRateAtSpecificDate() {
        ExchangeRateClient client = getClient();

        Assertions.assertEquals(new BigDecimal("31.9396"), client.getExchangeRate(LocalDate.of(2018, 10, 1)));
        Assertions.assertEquals(new BigDecimal("32.0679"), client.getExchangeRate(LocalDate.of(2018, 10, 31)));
    }

    @Test
    public void testMonthlyExchangeRateAtSpecificDate() {
        ExchangeRateClient client = getClient();
        MonthlyExchangeRate monthlyExchangeRate = client.getMonthlyExchangeRate(2018, 10);

        Assertions.assertEquals(31, monthlyExchangeRate.size());
        Assertions.assertEquals(new BigDecimal("31.9396"), monthlyExchangeRate.getFirstExchangeRate());
        Assertions.assertEquals(new BigDecimal("32.0679"), monthlyExchangeRate.getLastExchangeRate());
        Assertions.assertEquals(new BigDecimal("31.9994"), monthlyExchangeRate.getExchangeRate(LocalDate.of(2018, 10, 15)));
        Assertions.assertEquals(BigDecimal.ZERO, monthlyExchangeRate.getExchangeRate(LocalDate.of(2018, 9, 30)));
        Assertions.assertFalse(monthlyExchangeRate.isIncomplete());

        LocalDate date1 = LocalDate.of(2018, 10, 1);
        LocalDate date2 = LocalDate.of(2018, 10, 15);
        Map<LocalDate, BigDecimal> rangeOfValues = monthlyExchangeRate.getExchangeRateBetween(date1, date2);
        Assertions.assertEquals(15, rangeOfValues.size());
        Assertions.assertEquals(new BigDecimal("31.9396"), rangeOfValues.get(date1));
        Assertions.assertEquals(new BigDecimal("31.9994"), rangeOfValues.get(date2));
    }

    @Test
    public void testValidationOfYear() {
        ExchangeRateClient client = getClient();

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            client.getExchangeRate(LocalDate.of(2011, 12, 31));
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            client.getMonthlyExchangeRate(2011, Month.DECEMBER);
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            client.getMonthlyExchangeRate(2011, 10);
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            client.getMonthlyExchangeRate(LocalDate.of(2011, 12, 1));
        });
    }

}
