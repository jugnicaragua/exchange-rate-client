package ni.jug.exchangerate;

import ni.jug.util.Inputs;
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
public final class CentralBankScraper {

    private static final Logger LOGGER = Logger.getLogger(CentralBankScraper.class.getName());

    public static final int MINIMUM_YEAR = 2012;

    public static final String BCN_EXCHANGE_RATE_URL = "https://www.bcn.gob.ni/IRR/tipo_cambio_mensual/mes.php";
    public static final String QUERY_STRING = "mes=%02d&anio=%d";

    public static final String ERROR_DOM_CHANGED = "El DOM de la pagina web del BCN tiene un formato diferente al esperado";
    public static final String ERROR_BCN_CONNECTION = "Error durante la conexion al sitio web del BCN";
    public static final String ERROR_YEAR_OUT_OF_BOUNDS = "El año de consulta [%d] debe estar entre [%d, %d] inclusive";

    public static MonthlyExchangeRate getMonthlyExchangeRate(YearMonth yearMonth, int maxRetryCount) throws ExchangeRateException {
        Objects.requireNonNull(yearMonth);
        Inputs.numberInRange(maxRetryCount, 1, 10);
        yearInRange(yearMonth);

        MonthlyExchangeRate monthlyExchangeRate = null;
        ExchangeRateException error = null;
        boolean fetched = false;
        int count = 1;

        do {
            try {
                LOGGER.log(Level.INFO, "Peticion [{0}]: Importar tasas del sitio web del BCN", count);
                monthlyExchangeRate = new MonthlyExchangeRate(fetchExchangeRateData(yearMonth));
                fetched = true;
            } catch (ExchangeRateException ex) {
                error = ex;
            }
        } while (!fetched && ++count <= maxRetryCount);

        if (!fetched) {
            throw error;
        }

        return monthlyExchangeRate;
    }

    private static void yearInRange(YearMonth yearMonth) {
        LocalDate now = LocalDate.now();
        int maximumYear = now.getYear();

        if (now.getMonth() == Month.DECEMBER && yearMonth.getYear() == (now.getYear() + 1) && yearMonth.getMonth() == Month.JANUARY) {
            ++maximumYear;
        }
        if (yearMonth.getYear() < MINIMUM_YEAR || yearMonth.getYear() > maximumYear) {
            String msg = String.format(ERROR_YEAR_OUT_OF_BOUNDS, yearMonth.getYear(), MINIMUM_YEAR, maximumYear);
            throw new IllegalArgumentException(msg);
        }
    }

    private static String buildURL(YearMonth yearMonth) {
        return new StringBuilder(BCN_EXCHANGE_RATE_URL)
                .append('?')
                .append(String.format(QUERY_STRING, yearMonth.getMonthValue(), yearMonth.getYear()))
                .toString();
    }

    private static TreeMap<LocalDate, BigDecimal> fetchExchangeRateData(YearMonth yearMonth) throws ExchangeRateException {
        try {
            String centralBankURL = buildURL(yearMonth);
            Document doc = Jsoup.connect(centralBankURL)
                    .get();
            Elements divs = doc.select("tbody div[align]");

            if (divs.size() <= 2) {
                throw new ExchangeRateException(ERROR_DOM_CHANGED);
            }

            Iterator<Element> itr = divs.iterator();
            // Omitir los 2 primeros elementos
            itr.next();
            itr.next();

            TreeMap<LocalDate, BigDecimal> exchangeRates = new TreeMap<>();
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
                    exchangeRates.put(date, value);
                    date = null;
                    value = null;
                }
            }

            return exchangeRates;
        } catch (IOException ioe) {
            throw new ExchangeRateException(ERROR_BCN_CONNECTION, ioe);
        }
    }
}
