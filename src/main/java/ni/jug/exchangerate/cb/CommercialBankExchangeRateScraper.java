package ni.jug.exchangerate.cb;

import ni.jug.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author Armando Alaniz
 * @version 1.0
 * @since 1.0
 */
public interface CommercialBankExchangeRateScraper {

    String ERROR_FOR_CONNECTING_TO_WEBSITE = "Error de conexion: No se pudo obtener el contenido del sitio web de [%s]";
    String ERROR_FOR_PARSING_TEXT = "No se pudo extraer el dato: el sitio web de [%s] presenta contenido inesperado: %s";
    String ERROR_FOR_READING_HTML = "No se pudo extraer el dato: el HTML del sitio web de [%s] ha sido modificado";

    String bank();

    String description();

    String url();

    ExchangeRateTrade extractData();

    default Document makeGetRequest() {
        try {
            return Jsoup.connect(url())
                    .validateTLSCertificates(false)
                    .cookies(ExecutionContext.getInstance().cookies(bank()))
                    .get();
        } catch (IOException ioe) {
            throw newConnectionError(ioe);
        }
    }

    default Elements selectExchangeRateElements(int expectedMinimumSize, String cssSelector) {
        Document doc = makeGetRequest();
        Elements elements = doc.select(cssSelector);
        if (elements.size() < expectedMinimumSize) {
            throw new IllegalArgumentException(String.format(ERROR_FOR_READING_HTML, bank()));
        }
        return elements;
    }

    default String fetchAsPlainText() {
        try {
            return Jsoup.connect(url())
                    .validateTLSCertificates(false)
                    .cookies(ExecutionContext.getInstance().cookies(bank()))
                    .ignoreContentType(true)
                    .execute()
                    .body();
        } catch (IOException ioe) {
            throw newConnectionError(ioe);
        }
    }

    default ExchangeRateTrade extractDataFromContent(String content, String leftBuy, String rightBuy, String leftSell, String rightSell,
            String offset) {
        String buyText = Strings.substringBetween(content, leftBuy, rightBuy, offset);
        if (buyText.isEmpty()) {
            throw newParsingError(content);
        }
        BigDecimal buy = new BigDecimal(buyText).setScale(4);

        String sellText = Strings.substringBetween(content, leftSell, rightSell, leftBuy + buyText + rightBuy);
        if (sellText.isEmpty()) {
            throw newParsingError(content);
        }
        BigDecimal sell = new BigDecimal(sellText).setScale(4);

        return new ExchangeRateTrade(bank(), buy, sell);
    }

    default ExchangeRateTrade extractDataFromPlainTextResponse(String leftBuy, String rightBuy, String leftSell, String rightSell,
            String offset) {
        String response = fetchAsPlainText();
        return extractDataFromContent(response, leftBuy, rightBuy, leftSell, rightSell, offset);
    }

    default ExchangeRateTrade extractDataFromPlainTextResponse(String open, String close) {
        return extractDataFromPlainTextResponse(open, close, open, close, null);
    }

    default BigDecimal parseText(String value, String offset) {
        String exchangeRateText = (offset == null || offset.isEmpty()) ? value : Strings.substringAfter(value, offset);
        if (exchangeRateText.isEmpty()) {
            throw newParsingError(value);
        }
        return new BigDecimal(exchangeRateText).setScale(4);
    }

    default BigDecimal parseText(String value) {
        return parseText(value, null);
    }

    default IllegalArgumentException newParsingError(String value) {
        return new IllegalArgumentException(String.format(ERROR_FOR_PARSING_TEXT, bank(), value));
    }

    default IllegalArgumentException newConnectionError(IOException ioe) {
        return new IllegalArgumentException(String.format(ERROR_FOR_CONNECTING_TO_WEBSITE, bank()), ioe);
    }

}
