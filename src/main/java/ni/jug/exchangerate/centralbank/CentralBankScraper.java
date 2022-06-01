package ni.jug.exchangerate.centralbank;

import ni.jug.exchangerate.ExchangeRateException;
import ni.jug.exchangerate.RetryExchangeRateException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author aalaniz
 */
public class CentralBankScraper implements CentralBankQuery {

    private static final Logger LOGGER = Logger.getLogger(CentralBankScraper.class.getName());

    public static final CentralBankQuery INSTANCE = new CentralBankScraper();

    public static final String BCN_EXCHANGE_RATE_URL = "https://www.bcn.gob.ni/IRR/tipo_cambio_mensual/mes.php?mes=%02d&anio=%d";

    public static final String ERROR_DOM_CHANGED = "El DOM de la pagina web del BCN tiene un formato diferente al esperado.";
    public static final String ERROR_BCN_CONNECTION = "Error durante la conexion al sitio web del BCN.";

    private CentralBankScraper() {
    }

    @Override
    public MonthlyExchangeRate getMonthlyExchangeRate(YearMonth period) throws ExchangeRateException {
        PeriodValidator.INSTANCE.validatePeriod(Objects.requireNonNull(period));

        LOGGER.log(Level.INFO, "BCN: importar tipo de cambio para el periodo {0}", period);

        Map<LocalDate, BigDecimal> exchangeRates = crawlExchangeRates(period.getYear(), period.getMonthValue(), -1);
        return MonthlyExchangeRate.create(new TreeMap<>(exchangeRates));
    }

    @Override
    public BigDecimal getExchangeRate(LocalDate date) throws ExchangeRateException {
        PeriodValidator.INSTANCE.validatePeriod(Objects.requireNonNull(date));

        LOGGER.log(Level.INFO, "BCN: importar tipo de cambio para la fecha {0}", date);

        Map<LocalDate, BigDecimal> exchangeRates = crawlExchangeRates(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        return exchangeRates.getOrDefault(date, BigDecimal.ZERO);
    }

    private Map<LocalDate, BigDecimal> crawlExchangeRates(int year, int month, int day) throws ExchangeRateException {
        String exchangeRateURL = String.format(BCN_EXCHANGE_RATE_URL, month, year);

        try {
            Document doc = Jsoup.connect(exchangeRateURL).get();
            Elements divs = doc.select("tbody div[align]");

            if (divs.size() <= 2) {
                throw new RetryExchangeRateException(ERROR_DOM_CHANGED);
            }

            Iterator<Element> itr = divs.iterator();
            // Omitir los 2 primeros elementos
            itr.next();
            itr.next();

            Map<LocalDate, BigDecimal> exchangeRates = new LinkedHashMap<>(31);
            LocalDate date = null;
            BigDecimal value = null;
            while (itr.hasNext()) {
                String text = itr.next().text();

                if (text.contains("-")) {
                    date = LocalDate.of(year, month, Integer.parseInt(text.substring(0, 2)));
                } else {
                    value = new BigDecimal(text);
                }

                if (date != null && value != null) {
                    if (day < 1) {
                        exchangeRates.put(date, value);
                    } else if (date.getDayOfMonth() == day) {
                        exchangeRates.put(date, value);
                        break;
                    }

                    date = null;
                    value = null;
                }
            }

            return exchangeRates;
        } catch (IOException ioe) {
            throw new RetryExchangeRateException(ERROR_BCN_CONNECTION, ioe);
        }
    }
}
