package ni.jug.cb.exchangerate;

import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author Armando Alaniz
 * @version 1.0
 * @since 1.0
 */
public interface ExchangeRateScraper {

    String ERROR_FOR_PARSING_TEXT = "No se pudo extraer el dato de [%s]";
    String ERROR_FOR_READING_HTML = "No se pudo extraer el dato, el HTML del sitio web de [%s] ha sido modificado";

    String bank();

    String url();

    String cssSelector();

    ExchangeRateTrade extractData();

    default Document makeGetRequest() {
        try {
            return Jsoup.connect(url())
                    .validateTLSCertificates(false)
                    .get();
        } catch (IOException ioe) {
            throw new IllegalArgumentException("No se pudo obtener el contenido del sitio web de [" + bank() + "]", ioe);
        }
    }

    default Elements selectExchangeRateElements(int expectedMinimumSize) {
        Document doc = makeGetRequest();
        Elements elements = doc.select(cssSelector());
        if (elements.size() < expectedMinimumSize) {
            throw new IllegalArgumentException(String.format(ERROR_FOR_READING_HTML, bank()));
        }
        return elements;
    }

    default String fetchAsPlainText() {
        try {
            return Jsoup.connect(url()).validateTLSCertificates(false).ignoreContentType(true).execute().body();
        } catch (IOException ioe) {
            throw new IllegalArgumentException("No se pudo obtener el contenido del sitio web de [" + bank() + "]", ioe);
        }
    }

    default void doThrowParsingError(String value) {
        throw new IllegalArgumentException(String.format(ERROR_FOR_PARSING_TEXT, value));
    }

}
