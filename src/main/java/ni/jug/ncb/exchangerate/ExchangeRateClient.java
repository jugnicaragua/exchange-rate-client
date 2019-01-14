package ni.jug.ncb.exchangerate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

/**
 *
 * @author Armando Alaniz
 * @version 2.0
 * @since 1.0
 */
public interface ExchangeRateClient {

    int MINIMUM_YEAR = 2012;

    default void doValidateYear(int year, Month month) {
        doValidateYear(LocalDate.of(year, month, 1));
    }

    default void doValidateYear(LocalDate date) {
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();

        if (now.getYear() >= MINIMUM_YEAR && now.getMonth() == Month.DECEMBER && date.getYear() == (now.getYear() + 1) &&
                date.getMonth() == Month.JANUARY) {
            ++currentYear;
        }
        if (date.getYear() < MINIMUM_YEAR || date.getYear() > currentYear) {
            throw new IllegalArgumentException("El año de consulta [" + date.getYear() + "] debe estar entre [" + MINIMUM_YEAR + ", " + currentYear +
                    "] inclusive");
        }
    }

    BigDecimal getExchangeRate(LocalDate date);

    default BigDecimal getCurrentExchangeRate() {
        return getExchangeRate(LocalDate.now());
    }

    MonthlyExchangeRate getMonthlyExchangeRate(int year, Month month);

    default MonthlyExchangeRate getMonthlyExchangeRate(int year, int month) {
        return getMonthlyExchangeRate(year, Month.of(month));
    }

    default MonthlyExchangeRate getMonthlyExchangeRate(LocalDate date) {
        return getMonthlyExchangeRate(date.getYear(), date.getMonth());
    }

    default MonthlyExchangeRate getCurrentMonthExchangeRate() {
        return getMonthlyExchangeRate(LocalDate.now());
    }

}
