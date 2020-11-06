package ni.jug.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 *
 * @author Armando Alaniz
 * @version 2.0
 * @since 1.0
 */
public final class Dates {

    public static final String ERROR_INVALID_DATE_RANGE = "La fecha1 [%s] debe ser menor o igual a fecha2 [%s]";

    private static final char HYPHEN = '-';

    private Dates() {
    }

    public static LocalDate getCurrentDateOrLastDayOf(LocalDate pastDate) {
        LocalDate now = LocalDate.now();
        return pastDate.compareTo(now) <= 0 ? now : LocalDate.of(pastDate.getYear(), pastDate.getMonth(), 1).plusMonths(1).minusDays(1);
    }

    public static LocalDate getLastDateOfMonthOf(LocalDate date) {
        return LocalDate.of(date.getYear(), date.getMonth(), 1).plusMonths(1).minusDays(1);
    }

    public static LocalDate toLocalDate(String value) {
        return LocalDate.parse(value, DateTimeFormatter.ISO_DATE);
    }

    public static LocalDate toFirstDateOfYearMonth(String yearMonth) {
        return LocalDate.parse(yearMonth + HYPHEN + "01", DateTimeFormatter.ISO_DATE);
    }

    public static void validateDateRange(LocalDate date1, LocalDate date2) {
        if (date1.isAfter(date2)) {
            String msg = String.format(ERROR_INVALID_DATE_RANGE, date1, date2);
            throw new IllegalArgumentException(msg);
        }
    }

    public static int daysInMonth(LocalDate date) {
        LocalDate firstDay = date.withDayOfMonth(1);
        LocalDate lastDay = firstDay.plusMonths(1);
        return (int) ChronoUnit.DAYS.between(firstDay, lastDay);
    }

    public static boolean between(LocalDate date, LocalDate start, LocalDate end) {
        Objects.requireNonNull(date);
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);
        return date.compareTo(start) >= 0 && date.compareTo(end) <= 0;
    }
}
