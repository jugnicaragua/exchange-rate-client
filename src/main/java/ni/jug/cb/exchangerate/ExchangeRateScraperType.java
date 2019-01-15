package ni.jug.cb.exchangerate;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Iterator;
import ni.jug.util.Strings;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Armando Alaniz
 * @version 1.0
 * @since 1.0
 */
public enum ExchangeRateScraperType implements ExchangeRateScraper {

    BANPRO("https://www.banprogrupopromerica.com.ni/umbraco/Surface/TipoCambio/Run?json={\"operacion\":2}") {
        private static final String OPEN_TAG = "\\u003cTD class=gris10px height=20 vAlign=middle width=75 align=center\\u003e";
        private static final String CLOSE_TAG = "\\u003c/TD\\u003e";

        @Override
        public String cssSelector() {
            throw new UnsupportedOperationException("La respuesta del sitio web de [" + bank() + "] es un JSON");
        }

        @Override
        public ExchangeRateTrade extractData() {
            String content = fetchAsPlainText();

            String buyText = Strings.substringBetween(content, OPEN_TAG, CLOSE_TAG);
            if (buyText.isEmpty()) {
                doThrowParsingError(content);
            }
            BigDecimal buy = new BigDecimal(buyText).setScale(4);

            String sellText = Strings.substringBetween(content, OPEN_TAG, CLOSE_TAG, OPEN_TAG + buyText + CLOSE_TAG);
            if (sellText.isEmpty()) {
                doThrowParsingError(content);
            }
            BigDecimal sell = new BigDecimal(sellText).setScale(4);

            return new ExchangeRateTrade(bank(), buy, sell);
        }
    }, FICOHSA("https://www.ficohsa.com/ni/nicaragua/tipo-de-cambio/") {
        private BigDecimal convertBuyValue(String value) {
            String buyText = Strings.substringAfter(value, "Compra: ");
            if (buyText.isEmpty()) {
                doThrowParsingError(value);
            }
            return new BigDecimal(buyText).setScale(4);
        }

        private BigDecimal convertSellValue(String value) {
            String sellText = Strings.substringAfter(value, "Venta: ");
            if (sellText.isEmpty()) {
                doThrowParsingError(value);
            }
            return new BigDecimal(sellText).setScale(4);
        }

        @Override
        public String cssSelector() {
            return "article > p > span";
        }

        @Override
        public ExchangeRateTrade extractData() {
            Elements spans = selectExchangeRateElements(2);

            Iterator<Element> itr = spans.iterator();
            BigDecimal buy = convertBuyValue(itr.next().text());
            BigDecimal sell = convertSellValue(itr.next().text());

            return new ExchangeRateTrade(bank(), buy, sell);
        }
    }, AVANZ("https://www.avanzbanc.com/Pages/Empresas/ServiciosFinancieros/MesaCambio.aspx") {
        @Override
        public String cssSelector() {
            return "#avanz-mobile-tipo-cambio > strong";
        }

        @Override
        public ExchangeRateTrade extractData() {
            Elements spans = selectExchangeRateElements(2);

            Iterator<Element> itr = spans.iterator();
            BigDecimal buy = new BigDecimal(itr.next().text()).setScale(4);
            BigDecimal sell = new BigDecimal(itr.next().text()).setScale(4);

            return new ExchangeRateTrade(bank(), buy, sell);
        }
    }, BAC("https://www.sucursalelectronica.com/redir/showLogin.go") {
        private static final String NIC_BLOCK_LITERAL = "countryCode : 'NI',";
        private static final String BUY_LITERAL = "buy : '";
        private static final String SELL_LITERAL = "sell : '";
        private static final String CLOSE_LITERAL = "',";

        @Override
        public String cssSelector() {
            return "script:not(script[type])";
        }

        @Override
        public ExchangeRateTrade extractData() {
            Elements scripts = selectExchangeRateElements(3);

            Iterator<Element> itr = scripts.iterator();
            itr.next();
            itr.next();
            Element script = itr.next();
            String scriptContent = script.html();

            String buyText = Strings.substringBetween(scriptContent, BUY_LITERAL, CLOSE_LITERAL, NIC_BLOCK_LITERAL);
            if (buyText.isEmpty()) {
                doThrowParsingError(scriptContent);
            }
            BigDecimal buy = new BigDecimal(buyText).setScale(4);

            String sellText = Strings.substringBetween(scriptContent, SELL_LITERAL, CLOSE_LITERAL, BUY_LITERAL + buyText + CLOSE_LITERAL);
            if (sellText.isEmpty()) {
                doThrowParsingError(scriptContent);
            }
            BigDecimal sell = new BigDecimal(sellText).setScale(4);

            return new ExchangeRateTrade(bank(), buy, sell);
        }
    }, BDF("https://www.bdfnet.com/") {
        private static final String UA_FIREFOX_V64 = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:64.0) Gecko/20100101 Firefox/64.0";

        @Override
        public Document makeGetRequest() {
            try {
                return Jsoup.connect(url())
                        .validateTLSCertificates(false)
                        .userAgent(UA_FIREFOX_V64)
                        .cookie("visid_incap_1796147", "m4r46cj3SVagBLl6ga4rWzCHNlwAAAAAQUIPAAAAAABJ/PEx6MFjfho367NgBxEl")
                        .cookie("incap_ses_1062_1796147", "bUaaPR6p/0OPBKmxvPu8DjCHNlwAAAAAb1aUF2soRvHhaHMyM1hESQ==")
                        .get();
            } catch (IOException ioe) {
                throw new IllegalArgumentException("No se pudo obtener el contenido del sitio web de [" + bank() + "]", ioe);
            }
        }

        @Override
        public String cssSelector() {
            return "#ctl00_ContentPlaceHolder1_wucHerramientas1_lblCompraDolar, " +
                    "#ctl00_ContentPlaceHolder1_wucHerramientas1_lblVentaDolar";
        }

        @Override
        public ExchangeRateTrade extractData() {
            Elements spans = selectExchangeRateElements(2);
            Iterator<Element> itr = spans.iterator();
            BigDecimal buy = new BigDecimal(itr.next().text()).setScale(4);
            BigDecimal sell = new BigDecimal(itr.next().text()).setScale(4);
            return new ExchangeRateTrade(bank(), buy, sell);
        }
    }, LAFISE("https://www.lafise.com/DesktopModules/Servicios/API/TasaCambio/VerPorPaisActivo") {
        private static final String OFFSET_TEXT = "\"Descripcion\":\"Córdoba - Dolar\"";
        private static final String BUY_LITERAL = "\"ValorCompra\":\"NIO: ";
        private static final String SELL_LITERAL = "\"ValorVenta\":\"USD: ";
        private static final String CLOSE_LITERAL = "\",";

        private final String payload;
        {
            StringBuilder data = new StringBuilder();
            data.append("{");
            data.append("\"Activo\": true,");
            data.append("\"Descripcion\": \"\",");
            data.append("\"IdPais\": -1,");
            data.append("\"PathUrl\": \"https://www.lafise.com/blb/\",");
            data.append("\"SimboloCompra\": \"\",");
            data.append("\"SimboloVenta\": \"\",");
            data.append("\"ValorCompra\": \"\",");
            data.append("\"ValorVenta\": \"\"");
            data.append("}");
            payload = data.toString();
        }

        @Override
        public String cssSelector() {
            throw new UnsupportedOperationException("La respuesta del sitio web de [" + bank() + "] es un JSON");
        }

        @Override
        public String fetchAsPlainText() {
            try {
                return Jsoup
                        .connect(url())
                        .validateTLSCertificates(false)
                        .header("Content-Type", "application/json;charset=UTF-8")
                        .requestBody(payload)
                        .method(Connection.Method.POST)
                        .ignoreContentType(true)
                        .execute()
                        .body();
            } catch (IOException ioe) {
                throw new IllegalArgumentException("No se pudo obtener el contenido del sitio web de [" + bank() + "]", ioe);
            }
        }

        @Override
        public ExchangeRateTrade extractData() {
            String content = fetchAsPlainText();

            String buyText = Strings.substringBetween(content, BUY_LITERAL, CLOSE_LITERAL, OFFSET_TEXT);
            if (buyText.isEmpty()) {
                doThrowParsingError(content);
            }
            BigDecimal buy = new BigDecimal(buyText).setScale(4);

            String sellText = Strings.substringBetween(content, SELL_LITERAL, CLOSE_LITERAL, BUY_LITERAL + buyText + CLOSE_LITERAL);
            if (sellText.isEmpty()) {
                doThrowParsingError(content);
            }
            BigDecimal sell = new BigDecimal(sellText).setScale(4);

            return new ExchangeRateTrade(bank(), buy, sell);
        }
    };

    private final String url;

    private ExchangeRateScraperType(String url) {
        this.url = url;
    }

    @Override
    public String bank() {
        return name();
    }

    @Override
    public String url() {
        return url;
    }

}
