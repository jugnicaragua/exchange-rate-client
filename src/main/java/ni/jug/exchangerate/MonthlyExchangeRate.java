package ni.jug.exchangerate;

import ni.jug.util.Dates;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeMap;

/**
 *
 * @author Armando Alaniz
 * @version 2.0
 * @since 1.0
 */
public final class MonthlyExchangeRate implements Iterable<Map.Entry<LocalDate, BigDecimal>> {

    public static final String ERROR_DATE_OUT_OF_BOUNDS = "Fecha fuera de rango. Rango esperado [%s, %s]";

    private final Map<LocalDate, BigDecimal> exchangeRates;
    private final LocalDate firstDate;
    private final LocalDate lastDate;
    private final boolean incomplete;
    private final int size;

    public MonthlyExchangeRate(TreeMap<LocalDate, BigDecimal> exchangeRates) {
        this.exchangeRates = Objects.requireNonNull(exchangeRates);
        if (this.exchangeRates.isEmpty()) {
            firstDate = null;
            lastDate = null;
            incomplete = true;
            size = 0;
        } else {
            NavigableSet<LocalDate> dates = (NavigableSet<LocalDate>) this.exchangeRates.keySet();
            firstDate = dates.first();
            lastDate = dates.last();
            incomplete = this.exchangeRates.size() != Dates.daysInMonth(firstDate);
            size = this.exchangeRates.size();
        }
    }

    public Map<LocalDate, BigDecimal> getMonthlyExchangeRate() {
        return Collections.unmodifiableMap(exchangeRates);
    }

    public BigDecimal getExchangeRate(LocalDate date) {
        Objects.requireNonNull(date);
        return exchangeRates.getOrDefault(date, BigDecimal.ZERO);
    }

    public Map<LocalDate, BigDecimal> getExchangeRateBetween(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);

        Dates.validateDateRange(start, end);

        if (Dates.between(start, firstDate, lastDate) && Dates.between(end, firstDate, lastDate)) {
            Map<LocalDate, BigDecimal> rangeOfValues = new TreeMap<>();
            while (start.compareTo(end) <= 0) {
                rangeOfValues.put(start, exchangeRates.getOrDefault(start, BigDecimal.ZERO));
                start = start.plusDays(1);
            }
            return Collections.unmodifiableMap(rangeOfValues);
        } else {
            String msg = String.format(ERROR_DATE_OUT_OF_BOUNDS, firstDate, lastDate);
            throw new IllegalArgumentException(msg);
        }
    }

    public BigDecimal getFirstExchangeRate() {
        return exchangeRates.getOrDefault(firstDate, BigDecimal.ZERO);
    }

    public BigDecimal getLastExchangeRate() {
        return exchangeRates.getOrDefault(lastDate, BigDecimal.ZERO);
    }

    public boolean isIncomplete() {
        return incomplete;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Iterator<Map.Entry<LocalDate, BigDecimal>> iterator() {
        return exchangeRates.entrySet().iterator();
    }

    @Override
    public String toString() {
        return "MonthlyExchangeRate{" + exchangeRates + '}';
    }
}
