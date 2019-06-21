package ni.jug.exchangerate.ncb;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;

/**
 *
 * @author Armando Alaniz
 * @version 2.0
 * @since 1.0
 */
public interface NiCentralBankExchangeRateClient {

    int MINIMUM_YEAR = 2012;

    default void doValidateYear(YearMonth yearMonth) {
        doValidateYear(LocalDate.of(yearMonth.getYear(), yearMonth.getMonthValue(), 1));
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

    MonthlyExchangeRate getMonthlyExchangeRate(YearMonth yearMonth);

    default MonthlyExchangeRate getMonthlyExchangeRate(LocalDate date) {
        return getMonthlyExchangeRate(YearMonth.from(date));
    }

    default MonthlyExchangeRate getCurrentMonthlyExchangeRate() {
        return getMonthlyExchangeRate(LocalDate.now());
    }

}
