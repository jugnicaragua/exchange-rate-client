package ni.jug.exchangerate.centralbank;

import ni.jug.exchangerate.ExchangeRateException;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;

/**
 * @author aalaniz
 */
public class PeriodValidator {

    public static final PeriodValidator INSTANCE = new PeriodValidator();

    public static final int MINIMUM_YEAR = 2012;

    public static final String ERROR_YEAR_OUT_OF_BOUNDS = "El año de consulta [%d] debe estar entre [%d, %d] inclusive.";

    private PeriodValidator() {
    }

    public void validatePeriod(YearMonth period) throws ExchangeRateException {
        int maxYear = getMaxYear(period);
        if (period.getYear() < MINIMUM_YEAR || period.getYear() > maxYear) {
            throw new ExchangeRateException(ERROR_YEAR_OUT_OF_BOUNDS, period.getYear(), MINIMUM_YEAR, maxYear);
        }
    }

    public void validatePeriod(LocalDate date) throws ExchangeRateException {
        validatePeriod(YearMonth.from(date));
    }

    private int getMaxYear(YearMonth period) {
        LocalDate now = LocalDate.now();
        int maxYear = now.getYear();

        // We are currently in December and user wants to fetch January's next year data
        if (now.getMonth() == Month.DECEMBER && period.getYear() == (now.getYear() + 1) && period.getMonth() == Month.JANUARY) {
            ++maxYear;
        }
        return maxYear;
    }
}
