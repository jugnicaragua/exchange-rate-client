package ni.jug.exchangerate.centralbank;

import ni.jug.util.Dates;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeMap;

/**
 *
 * @author aalaniz
 */
public final class MonthlyExchangeRate implements Iterable<Map.Entry<LocalDate, BigDecimal>> {

    public static final String ERROR_DATE_OUT_OF_BOUNDS = "Fecha fuera de rango. Rango esperado [%s, %s].";

    private final Map<LocalDate, BigDecimal> exchangeRates;
    private final LocalDate firstDate;
    private final LocalDate lastDate;
    private final boolean hasGaps;
    private final int size;

    private MonthlyExchangeRate(Map<LocalDate, BigDecimal> exchangeRates, LocalDate firstDate, LocalDate lastDate, boolean hasGaps,
            int size) {
        this.exchangeRates = exchangeRates;
        this.firstDate = firstDate;
        this.lastDate = lastDate;
        this.hasGaps = hasGaps;
        this.size = size;
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

    public boolean isHasGaps() {
        return hasGaps;
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

    public static MonthlyExchangeRate create(NavigableMap<LocalDate, BigDecimal> exchangeRates) {
        Objects.requireNonNull(exchangeRates);
        if (exchangeRates.isEmpty()) {
            return new MonthlyExchangeRate(Collections.emptyMap(), null, null, true, 0);
        } else {
            NavigableSet<LocalDate> dates = exchangeRates.navigableKeySet();
            LocalDate first = dates.first();
            LocalDate last = dates.last();
            boolean withGaps = exchangeRates.size() != Dates.daysInMonth(first);
            return new MonthlyExchangeRate(exchangeRates, first, last, withGaps, exchangeRates.size());
        }
    }

    public static MonthlyExchangeRate create() {
        return new MonthlyExchangeRate(Collections.emptyMap(), null, null, true, 0);
    }

    @Override
    public String toString() {
        return "MonthlyExchangeRate{" +
                "exchangeRates=" + exchangeRates +
                ", firstDate=" + firstDate +
                ", lastDate=" + lastDate +
                ", hasGaps=" + hasGaps +
                ", size=" + size +
                '}';
    }
}
