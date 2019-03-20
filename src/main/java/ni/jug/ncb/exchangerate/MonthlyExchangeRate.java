package ni.jug.ncb.exchangerate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import ni.jug.util.Dates;

/**
 *
 * @author Armando Alaniz
 * @version 2.0
 * @since 1.0
 */
public class MonthlyExchangeRate {

    private final Map<LocalDate, BigDecimal> valuesByDate;
    private final LocalDate firstDate;
    private final LocalDate lastDate;
    private final boolean isIncomplete;
    private final int size;

    public MonthlyExchangeRate(MonthlyExchangeRateDataReader monthlyData) {
        valuesByDate = monthlyData.processResult();
        if (valuesByDate.isEmpty()) {
            firstDate = null;
            lastDate = null;
            isIncomplete = false;
            size = 0;
        } else {
            LocalDate _date = valuesByDate.keySet().iterator().next();
            firstDate = _date.withDayOfMonth(1);
            lastDate = firstDate.plusMonths(1).minusDays(1);
            isIncomplete = valuesByDate.size() != ChronoUnit.DAYS.between(firstDate, lastDate.plusDays(1));
            size = valuesByDate.size();
        }
    }

    public Map<LocalDate, BigDecimal> getMonthlyExchangeRate() {
        return Collections.unmodifiableMap(valuesByDate);
    }

    public BigDecimal getExchangeRate(LocalDate date) {
        Objects.requireNonNull(date);
        return valuesByDate.getOrDefault(date, BigDecimal.ZERO);
    }

    public BigDecimal getExchangeRate() {
        return valuesByDate.getOrDefault(LocalDate.now(), BigDecimal.ZERO);
    }

    public Map<LocalDate, BigDecimal> getExchangeRateBetween(LocalDate date1, LocalDate date2) {
        Objects.requireNonNull(date1);
        Objects.requireNonNull(date2);

        Dates.validateDate1IsBeforeDate2(date1, date2);

        if (date1.compareTo(firstDate) >= 0 && date1.compareTo(lastDate) <= 0 &&
                date2.compareTo(firstDate) >= 0 && date2.compareTo(lastDate) <= 0) {
            Map<LocalDate, BigDecimal> rangeOfValues = new TreeMap<>();
            while (date1.compareTo(date2) <= 0) {
                rangeOfValues.put(date1, valuesByDate.getOrDefault(date1, BigDecimal.ZERO));
                date1 = date1.plusDays(1);
            }
            return Collections.unmodifiableMap(rangeOfValues);
        } else {
            return Collections.emptyMap();
        }
    }

    public BigDecimal getFirstExchangeRate() {
        return valuesByDate.getOrDefault(firstDate, BigDecimal.ZERO);
    }

    public BigDecimal getLastExchangeRate() {
        return valuesByDate.getOrDefault(lastDate, BigDecimal.ZERO);
    }

    public boolean isIncomplete() {
        return isIncomplete;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public String toString() {
        return "MonthlyExchangeRate{" + valuesByDate + '}';
    }

}
