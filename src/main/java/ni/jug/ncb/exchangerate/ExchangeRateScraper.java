package ni.jug.ncb.exchangerate;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Objects;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author Armando Alaniz
 * @version 1.0
 * @since 2.0
 */
public class ExchangeRateScraper implements ExchangeRateClient {

    private static final String URL_EXCHANGE_RATE = "https://www.bcn.gob.ni/estadisticas/mercados_cambiarios/tipo_cambio/" +
            "cordoba_dolar/mes.php?";
    private static final String QUERY_STRING = "mes=%02d&anio=%d";

    private static String buildURL(int year, int month) {
        return new StringBuilder(URL_EXCHANGE_RATE).append(String.format(QUERY_STRING, month, year)).toString();
    }

    private static MonthlyExchangeRateHTMLDataReader createHTMLReader(int year, Month month) {
        try {
            Document doc = Jsoup.connect(buildURL(year, month.getValue())).validateTLSCertificates(false).get();
            return new MonthlyExchangeRateHTMLDataReader(doc, year, month);
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
        return new MonthlyExchangeRate(createHTMLReader(year, month));
    }

}
