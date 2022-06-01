package ni.jug.exchangerate.bank;

import ni.jug.exchangerate.ExchangeRateException;
import ni.jug.util.Strings;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author aalaniz
 */
public enum BankScraper {

    BANPRO("Banco de la Produccion", "https://www.banprogrupopromerica.com.ni/umbraco/Surface/TipoCambio/Run?" +
            "json=%7B%22operacion%22%3A2%7D") {
        private static final String OPEN_TAG = "\\u003cTD class=gris10px height=20 vAlign=middle width=75 align=center\\u003e";
        private static final String CLOSE_TAG = "\\u003c/TD\\u003e";

        private final Delimiter DELIMITER = new Delimiter(OPEN_TAG, CLOSE_TAG);

        @Override
        public ExchangeRateTrade fetchData() throws ExchangeRateException {
            return fetchData(DELIMITER);
        }

    }, FICOHSA("https://www.ficohsa.com/ni/nicaragua/tipo-de-cambio/") {
        @Override
        public ExchangeRateTrade fetchData() throws ExchangeRateException {
            Elements spans = queryCssSelector(2, "article > p > span");

            Iterator<Element> itr = spans.iterator();
            BigDecimal buy = parseText(itr.next().text(), "Compra: ");
            BigDecimal sell = parseText(itr.next().text(), "Venta: ");

            return new ExchangeRateTrade(bank(), buy, sell);
        }

    }, AVANZ("https://www.avanzbanc.com/Pages/Empresas/ServiciosFinancieros/MesaCambio.aspx") {
        @Override
        public ExchangeRateTrade fetchData() throws ExchangeRateException {
            Elements spans = queryCssSelector(2, "#avanz-mobile-tipo-cambio > strong");

            Iterator<Element> itr = spans.iterator();
            BigDecimal buy = parseText(itr.next().text());
            BigDecimal sell = parseText(itr.next().text());

            return new ExchangeRateTrade(bank(), buy, sell);
        }

    }, BAC("Banco de America Central", "https://www.sucursalelectronica.com/redir/showLogin.go") {
        private static final String NIC_BLOCK_LITERAL = "countryCode : 'NI',";

        private final Delimiter buyDelimiter = new Delimiter("buy : '", "',");
        private final Delimiter sellDelimiter = new Delimiter("sell : '", "',");

        @Override
        public ExchangeRateTrade fetchData() throws ExchangeRateException {
            Elements scripts = queryCssSelector(5, "script:not(script[type])");

            Iterator<Element> itr = scripts.iterator();
            itr.next();
            itr.next();
            itr.next();
            itr.next();
            Element script = itr.next();

            return extractDataFromHTML(script.html(), buyDelimiter, sellDelimiter, NIC_BLOCK_LITERAL);
        }

    }, BDF("Banco de Finanzas", "https://www.bdfnet.com/") {
        private static final String UA_FIREFOX_V64 = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:64.0) Gecko/20100101 Firefox/64.0";

        @Override
        Document getRequest() throws ExchangeRateException {
            try {
                return Jsoup.connect(url())
                        .cookies(ExecutionContext.getInstance().cookies(bank()))
                        .userAgent(UA_FIREFOX_V64)
                        .get();
            } catch (IOException ioe) {
                throw connectionError(ioe);
            }
        }

        @Override
        public ExchangeRateTrade fetchData() throws ExchangeRateException {
            Elements spans = queryCssSelector(2, "#ctl00_ContentPlaceHolder1_wucHerramientas1_lblCompraDolar, " +
                    "#ctl00_ContentPlaceHolder1_wucHerramientas1_lblVentaDolar");
            Iterator<Element> itr = spans.iterator();
            BigDecimal buy = parseText(itr.next().text());
            BigDecimal sell = parseText(itr.next().text());
            return new ExchangeRateTrade(bank(), buy, sell);
        }

    }, LAFISE("Latin American Financial Services", "https://www.lafise.com/DesktopModules/Servicios/API/TasaCambio/VerPorPaisActivo") {
        private static final String OFFSET_TEXT = "\"Córdoba - Dólar\",";

        private final Delimiter buyDelimiter = new Delimiter("\"ValorCompra\":\"NIO: ", "\",");
        private final Delimiter sellDelimiter = new Delimiter("\"ValorVenta\":\"USD: ", "\",");

        private final String payload;
        {
            StringBuilder data = new StringBuilder();
            data.append('{');
            data.append("\"Activo\": true,");
            data.append("\"Descripcion\": \"\",");
            data.append("\"IdPais\": -1,");
            data.append("\"PathUrl\": \"https://www.lafise.com/blb/\",");
            data.append("\"SimboloCompra\": \"\",");
            data.append("\"SimboloVenta\": \"\",");
            data.append("\"ValorCompra\": \"\",");
            data.append("\"ValorVenta\": \"\"");
            data.append('}');
            payload = data.toString();
        }

        @Override
        String getRequestAsTextPlain() throws ExchangeRateException {
            try {
                return Jsoup.connect(url())
                        .cookies(ExecutionContext.getInstance().cookies(bank()))
                        .header("Content-Type", "application/json;charset=UTF-8")
                        .requestBody(payload)
                        .method(Connection.Method.POST)
                        .ignoreContentType(true)
                        .execute()
                        .body();
            } catch (IOException ioe) {
                throw connectionError(ioe);
            }
        }

        @Override
        public ExchangeRateTrade fetchData() throws ExchangeRateException {
            return fetchData(buyDelimiter, sellDelimiter, OFFSET_TEXT);
        }
    };

    private static final Logger LOGGER = Logger.getLogger(BankScraper.class.getName());

    public static final String ERROR_BANK_CONNECTION = "Error durante la conexion al sitio web de [%s].";
    public static final String ERROR_PARSING_TEXT = "El DOM del sitio web de [%s] tiene un formato diferente al esperado: [%s].";
    public static final String ERROR_READING_HTML = "El DOM del sitio web de [%s] tiene un formato diferente al esperado.";

    private static final int BANK_COUNT = BankScraper.values().length;
    private static final List<Bank> BANKS = new ArrayList<>(BANK_COUNT);

    static {
        for (BankScraper scraper : BankScraper.values()) {
            BANKS.add(new Bank(scraper));
        }
    }

    private final String description;
    private final String url;

    BankScraper(String description, String url) {
        this.description = description;
        this.url = url;
    }

    BankScraper(String url) {
        this.description = name();
        this.url = url;
    }

    public String bank() {
        return name();
    }

    public String description() {
        return description;
    }

    public String url() {
        return url;
    }

    public abstract ExchangeRateTrade fetchData() throws ExchangeRateException;

    Document getRequest() throws ExchangeRateException {
        try {
            return Jsoup.connect(url())
                    .cookies(ExecutionContext.getInstance().cookies(bank()))
                    .get();
        } catch (IOException ioe) {
            throw connectionError(ioe);
        }
    }

    String getRequestAsTextPlain() throws ExchangeRateException {
        try {
            return Jsoup.connect(url())
                    .cookies(ExecutionContext.getInstance().cookies(bank()))
                    .ignoreContentType(true)
                    .execute()
                    .body();
        } catch (IOException ioe) {
            throw connectionError(ioe);
        }
    }

    Elements queryCssSelector(int expectedMinimumSize, String cssSelector) throws ExchangeRateException {
        Document doc = getRequest();
        Elements elements = doc.select(cssSelector);
        if (elements.size() < expectedMinimumSize) {
            throw readingHTMLError();
        }
        return elements;
    }

    ExchangeRateTrade fetchData(Delimiter delimiter) throws ExchangeRateException {
        return fetchData(delimiter, delimiter, null);
    }

    ExchangeRateTrade fetchData(Delimiter buyDelimiter, Delimiter sellDelimiter, String offset) throws ExchangeRateException {
        String response = getRequestAsTextPlain();
        return extractDataFromHTML(response, buyDelimiter, sellDelimiter, offset);
    }

    ExchangeRateTrade extractDataFromHTML(String response, Delimiter buyDelimiter, Delimiter sellDelimiter, String offset) throws ExchangeRateException {
        String buyText = Strings.substringBetween(response, buyDelimiter.left, buyDelimiter.right, offset);
        if (buyText.isEmpty()) {
            throw parsingTextError(response);
        }
        BigDecimal buy = new BigDecimal(buyText).setScale(4);

        String sellText = Strings.substringBetween(response, sellDelimiter.left, sellDelimiter.right, buyDelimiter.offset(buyText));
        if (sellText.isEmpty()) {
            throw parsingTextError(response);
        }
        BigDecimal sell = new BigDecimal(sellText).setScale(4);

        return new ExchangeRateTrade(bank(), buy, sell);
    }

    BigDecimal parseText(String value, String offset) throws ExchangeRateException {
        String exchangeRateText = (offset == null || offset.isEmpty()) ? value : Strings.substringAfter(value, offset);
        if (exchangeRateText.isEmpty()) {
            throw parsingTextError(value);
        }
        return new BigDecimal(exchangeRateText).setScale(4);
    }

    BigDecimal parseText(String value) throws ExchangeRateException {
        return parseText(value, null);
    }

    ExchangeRateException parsingTextError(String value) {
        return new ExchangeRateException(ERROR_PARSING_TEXT, bank(), value);
    }

    ExchangeRateException readingHTMLError() {
        return new ExchangeRateException(String.format(ERROR_READING_HTML, bank()));
    }

    ExchangeRateException connectionError(IOException ex) {
        return new ExchangeRateException(ERROR_BANK_CONNECTION, ex, bank());
    }

    public Callable<ExchangeRateTrade> createTask() {
        return () -> {
            try {
                return fetchData();
            } catch(ExchangeRateException ex) {
                LOGGER.severe(ex.getMessage());
                return null;
            }
        };
    }

    public static int count() {
        return BANK_COUNT;
    }

    public static List<Bank> banks() {
        return Collections.unmodifiableList(BANKS);
    }

    public static List<Callable<ExchangeRateTrade>> createAsyncTasks() {
        return Stream.of(values())
                .map(BankScraper::createTask)
                .collect(Collectors.toList());
    }

    private static class Delimiter {
        private final String left;
        private final String right;

        public Delimiter(String left, String right) {
            this.left = left;
            this.right = right;
        }

        String offset(String value) {
            return new StringBuilder().append(left).append(value).append(right).toString();
        }
    }
}
