package ni.jug.exchangerate.ncb;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

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

    private static String buildURL(int year, int month) {
        return new StringBuilder(URL_EXCHANGE_RATE).append(String.format(QUERY_STRING, month, year)).toString();
    }

    private static ExchangeRateHTMLDataReader createHTMLReader(int year, Month month) {
        try {
            Document doc = Jsoup.connect(buildURL(year, month.getValue())).validateTLSCertificates(false).get();
            return new ExchangeRateHTMLDataReader(doc, year, month);
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
    public MonthlyExchangeRate getMonthlyExchangeRate(int year, Month month) {
        Objects.requireNonNull(month);
        doValidateYear(year, month);

        MonthlyExchangeRate monthlyExchangeRate = null;
        IllegalArgumentException error = null;
        boolean fetched = false;
        int count = 1;

        do {
            try {
                LOGGER.log(Level.INFO, "Peticion [{0}]: Importar datos del sitio web del BCN", count);
                monthlyExchangeRate = MonthlyExchangeRate.create(createHTMLReader(year, month));
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
