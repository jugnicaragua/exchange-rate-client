package ni.jug.ncb.exchangerate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import ni.jug.ncb.exchangerate.ws.RecuperaTCMesResponse;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 *
 * @author Armando Alaniz
 * @version 1.0
 * @since 2.0
 */
public class MonthlyExchangeRateWSDataReader implements MonthlyExchangeRateDataReader {

    private final RecuperaTCMesResponse.RecuperaTCMesResult wsResponse;

    public MonthlyExchangeRateWSDataReader(RecuperaTCMesResponse.RecuperaTCMesResult wsResponse) {
        this.wsResponse = wsResponse;
    }

    @Override
    public Map<LocalDate, BigDecimal> processResult() {
        if (wsResponse.getContent().isEmpty()) {
            return Collections.emptyMap();
        }

        Map<LocalDate, BigDecimal> valuesByDate = new TreeMap<>();
        Element root = (Element) wsResponse.getContent().get(0);
        Node exchangeRateNode = root.getFirstChild();
        while (exchangeRateNode != null) {
            String dateStr = null;
            String value = null;

            Node child = exchangeRateNode.getFirstChild();
            while (child != null) {
                if ("Fecha".equals(child.getNodeName())) {
                    dateStr = ((Text) child.getFirstChild()).getData();
                } else if ("Valor".equals(child.getNodeName())) {
                    value = ((Text) child.getFirstChild()).getData();
                }

                if (dateStr != null && value != null) {
                    break;
                }

                child = child.getNextSibling();
            }

            if (dateStr != null && value != null) {
                LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);
                valuesByDate.put(date, new BigDecimal(value));
            }

            exchangeRateNode = exchangeRateNode.getNextSibling();
        }

        return valuesByDate;
    }

}
