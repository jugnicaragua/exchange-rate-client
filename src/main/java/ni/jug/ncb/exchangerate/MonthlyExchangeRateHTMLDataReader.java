package ni.jug.ncb.exchangerate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Armando Alaniz
 * @version 1.0
 * @since 2.0
 */
public class MonthlyExchangeRateHTMLDataReader implements MonthlyExchangeRateDataReader {

    private final Document exchangeRateDoc;
    private final int year;
    private final Month month;

    public MonthlyExchangeRateHTMLDataReader(Document exchangeRateDoc, int year, Month month) {
        this.exchangeRateDoc = exchangeRateDoc;
        this.year = year;
        this.month = month;
    }

    @Override
    public Map<LocalDate, BigDecimal> processResult() {
        Elements divs = exchangeRateDoc.select("tbody div[align]");

        if (divs.isEmpty() || divs.size() <= 2) {
            return Collections.emptyMap();
        }

        Iterator<Element> itr = divs.iterator();
        // Omitir los 2 primeros elementos
        itr.next();
        itr.next();

        Map<LocalDate, BigDecimal> valuesByDate = new TreeMap<>();
        LocalDate date = null;
        BigDecimal value = null;
        String yearAndMonthValue = year + "-" + String.format("%02d", month.getValue()) + "-";
        while (itr.hasNext()) {
            String text = itr.next().text();

            if (text.contains("-")) {
                date = LocalDate.parse(yearAndMonthValue + text.substring(0, 2), DateTimeFormatter.ISO_DATE);
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
    }

}
