package ni.jug.exchangerate.ncb;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
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
 * @version 1.0
 * @since 2.0
 */
public class NiCentralBankExchangeRateScraper implements NiCentralBankExchangeRateClient {

    private static final Logger LOGGER = Logger.getLogger(NiCentralBankExchangeRateScraper.class.getName());

    private static final String URL_EXCHANGE_RATE = "https://www.bcn.gob.ni/estadisticas/mercados_cambiarios/tipo_cambio/" +
            "cordoba_dolar/mes.php?";
    private static final String QUERY_STRING = "mes=%02d&anio=%d";

    private static String buildURL(YearMonth yearMonth) {
        return new StringBuilder(URL_EXCHANGE_RATE).append(String.format(QUERY_STRING, yearMonth.getMonthValue(),
                yearMonth.getYear())).toString();
    }

    private static Map<LocalDate, BigDecimal> scrapExchangeRateInfo(YearMonth yearMonth) {
        try {
            Document doc = Jsoup.connect(buildURL(yearMonth)).validateTLSCertificates(false).get();
            Elements divs = doc.select("tbody div[align]");

            if (divs.isEmpty() || divs.size() <= 2) {
                throw new IllegalArgumentException("No se pudo obtener el dato, el dom de la pagina web del BCN ha cambiado");
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
            throw new IllegalArgumentException("No se pudieron extraer los datos del sitio web del BCN", ioe);
        }
    }

    @Override
    public BigDecimal getExchangeRate(LocalDate date) {
        MonthlyExchangeRate monthlyExchangeRate = getMonthlyExchangeRate(date);
        return monthlyExchangeRate.getExchangeRate(date);
    }

    @Override
    public MonthlyExchangeRate getMonthlyExchangeRate(YearMonth yearMonth) {
        Objects.requireNonNull(yearMonth);
        doValidateYear(yearMonth);

        MonthlyExchangeRate monthlyExchangeRate = null;
        IllegalArgumentException error = null;
        boolean fetched = false;
        int count = 1;

        do {
            try {
                LOGGER.log(Level.INFO, "Peticion [{0}]: Importar datos del sitio web del BCN", count);
                monthlyExchangeRate = new MonthlyExchangeRate(scrapExchangeRateInfo(yearMonth));
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

}
