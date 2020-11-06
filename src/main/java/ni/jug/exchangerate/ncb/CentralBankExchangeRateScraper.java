package ni.jug.exchangerate.ncb;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Armando Alaniz
 * @version 2.0
 * @since 2.0
 */
public class CentralBankExchangeRateScraper {

    private static final Logger LOGGER = Logger.getLogger(CentralBankExchangeRateScraper.class.getName());

    public static final int MINIMUM_YEAR = 2012;

    public static final String BCN_EXCHANGE_RATE_URL = "https://www.bcn.gob.ni/estadisticas/mercados_cambiarios/tipo_cambio/" +
            "cordoba_dolar/mes.php?";
    private static final String QUERY_STRING = "mes=%02d&anio=%d";

    public static BigDecimal getExchangeRateByDate(LocalDate date) {
        MonthlyExchangeRate monthlyExchangeRate = getMonthlyExchangeRate(date);
        return monthlyExchangeRate.getExchangeRate(date);
    }

    public static MonthlyExchangeRate getMonthlyExchangeRate(LocalDate date) {
        return getMonthlyExchangeRate(YearMonth.from(date));
    }

    public static MonthlyExchangeRate getMonthlyExchangeRate(YearMonth yearMonth) {
        Objects.requireNonNull(yearMonth);
        doValidateYear(yearMonth);

        MonthlyExchangeRate monthlyExchangeRate = null;
        IllegalArgumentException error = null;
        boolean fetched = false;
        int count = 1;

        do {
            try {
                LOGGER.log(Level.INFO, "Peticion [{0}]: Importar tasas del sitio web del BCN", count);
                monthlyExchangeRate = new MonthlyExchangeRate(fetchExchangeRateData(yearMonth));
                fetched = true;
            } catch (IllegalArgumentException iae) {
                error = iae;
            }
        } while (!fetched && ++count <= 3);

        if (!fetched) {
            throw error;
        }

        return monthlyExchangeRate;
    }

    private static void doValidateYear(YearMonth yearMonth) {
        LocalDate now = LocalDate.now();
        int maximumYear = now.getYear();

        if (now.getMonth() == Month.DECEMBER && yearMonth.getYear() == (now.getYear() + 1) && yearMonth.getMonth() == Month.JANUARY) {
            ++maximumYear;
        }
        if (yearMonth.getYear() < MINIMUM_YEAR || yearMonth.getYear() > maximumYear) {
            throw new IllegalArgumentException("El año de consulta [" + yearMonth.getYear() + "] debe estar entre [" + MINIMUM_YEAR +
                    ", " + maximumYear + "] inclusive");
        }
    }

    private static String buildURL(YearMonth yearMonth) {
        return new StringBuilder(BCN_EXCHANGE_RATE_URL)
                .append(String.format(QUERY_STRING, yearMonth.getMonthValue(), yearMonth.getYear()))
                .toString();
    }

    private static Map<LocalDate, BigDecimal> fetchExchangeRateData(YearMonth yearMonth) {
        try {
            String centralBankURL = buildURL(yearMonth);
            Document doc = Jsoup.connect(centralBankURL)
                    .validateTLSCertificates(false)
                    .get();
            Elements divs = doc.select("tbody div[align]");

            if (divs.size() <= 2) {
                throw new IllegalArgumentException("El DOM de la pagina web del BCN tiene un formato diferente al esperado");
            }

            Iterator<Element> itr = divs.iterator();
            // Omitir los 2 primeros elementos
            itr.next();
            itr.next();

            Map<LocalDate, BigDecimal> valuesByDate = new TreeMap<>();
            LocalDate date = null;
            BigDecimal value = null;
            while (itr.hasNext()) {
                String text = itr.next().text();

                if (text.contains("-")) {
                    date = LocalDate.of(yearMonth.getYear(), yearMonth.getMonthValue(), Integer.parseInt(text.substring(0, 2)));
                } else {
                    value = new BigDecimal(text);
                }

                if (date != null && value != null) {
                    valuesByDate.put(date, value);
                    date = null;
                    value = null;
                }
            }

            return valuesByDate;
        } catch (IOException ioe) {
            throw new IllegalArgumentException("Error durante la conexion al sitio web del BCN", ioe);
        }
    }
}
