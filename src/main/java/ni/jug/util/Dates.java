package ni.jug.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author Armando Alaniz
 * @version 1.0
 * @since 1.0
 */
public final class Dates {

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

    public static void validateDate1IsBeforeDate2(LocalDate date1, LocalDate date2) throws IllegalArgumentException {
        if (date1.isAfter(date2)) {
            throw new IllegalArgumentException("Rango de fecha invalido: la fecha1 debe ser menor o igual a fecha2");
        }
    }

}
