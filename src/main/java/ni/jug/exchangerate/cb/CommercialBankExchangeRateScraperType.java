package ni.jug.exchangerate.cb;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Armando Alaniz
 * @version 2.0
 * @since 1.0
 */
public enum CommercialBankExchangeRateScraperType implements CommercialBankExchangeRateScraper {

    BANPRO("Banco de la Produccion", "https://www.banprogrupopromerica.com.ni/umbraco/Surface/TipoCambio/Run?json={\"operacion\":2}") {
        private static final String OPEN_TAG = "\\u003cTD class=gris10px height=20 vAlign=middle width=75 align=center\\u003e";
        private static final String CLOSE_TAG = "\\u003c/TD\\u003e";

        @Override
        public ExchangeRateTrade extractData() {
            return extractDataFromPlainTextResponse(OPEN_TAG, CLOSE_TAG);
        }

    }, FICOHSA("https://www.ficohsa.com/ni/nicaragua/tipo-de-cambio/") {
        @Override
        public ExchangeRateTrade extractData() {
            Elements spans = selectExchangeRateElements(2, "article > p > span");

            Iterator<Element> itr = spans.iterator();
            BigDecimal buy = parseText(itr.next().text(), "Compra: ");
            BigDecimal sell = parseText(itr.next().text(), "Venta: ");

            return new ExchangeRateTrade(bank(), buy, sell);
        }

    }, AVANZ("https://www.avanzbanc.com/Pages/Empresas/ServiciosFinancieros/MesaCambio.aspx") {
        @Override
        public ExchangeRateTrade extractData() {
            Elements spans = selectExchangeRateElements(2, "#avanz-mobile-tipo-cambio > strong");

            Iterator<Element> itr = spans.iterator();
            BigDecimal buy = parseText(itr.next().text());
            BigDecimal sell = parseText(itr.next().text());

            return new ExchangeRateTrade(bank(), buy, sell);
        }

    }, BAC("Banco de America Central", "https://www.sucursalelectronica.com/redir/showLogin.go") {
        private static final String NIC_BLOCK_LITERAL = "countryCode : 'NI',";
        private static final String BUY_LITERAL = "buy : '";
        private static final String SELL_LITERAL = "sell : '";
        private static final String CLOSE_LITERAL = "',";

        @Override
        public ExchangeRateTrade extractData() {
            Elements scripts = selectExchangeRateElements(5, "script:not(script[type])");

            Iterator<Element> itr = scripts.iterator();
            itr.next();
            itr.next();
            itr.next();
            itr.next();
            Element script = itr.next();

            return extractDataFromContent(script.html(), BUY_LITERAL, CLOSE_LITERAL, SELL_LITERAL, CLOSE_LITERAL, NIC_BLOCK_LITERAL);
        }

    }, BDF("Banco de Finanzas", "https://www.bdfnet.com/") {
        private static final String UA_FIREFOX_V64 = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:64.0) Gecko/20100101 Firefox/64.0";

        @Override
        public Document makeGetRequest() {
            try {
                return Jsoup.connect(url())
                        .validateTLSCertificates(false)
                        .cookies(ExecutionContext.getInstance().cookies(bank()))
                        .userAgent(UA_FIREFOX_V64)
                        .get();
            } catch (IOException ioe) {
                throw new IllegalArgumentException("No se pudo obtener el contenido del sitio web de [" + bank() + "]", ioe);
            }
        }

        @Override
        public ExchangeRateTrade extractData() {
            Elements spans = selectExchangeRateElements(2, "#ctl00_ContentPlaceHolder1_wucHerramientas1_lblCompraDolar, " +
                    "#ctl00_ContentPlaceHolder1_wucHerramientas1_lblVentaDolar");
            Iterator<Element> itr = spans.iterator();
            BigDecimal buy = parseText(itr.next().text());
            BigDecimal sell = parseText(itr.next().text());
            return new ExchangeRateTrade(bank(), buy, sell);
        }

    }, LAFISE("Latin American Financial Services", "https://www.lafise.com/DesktopModules/Servicios/API/TasaCambio/VerPorPaisActivo") {
        private static final String OFFSET_TEXT = "\"Descripcion\":\"Córdoba - Dolar\"";
        private static final String BUY_LITERAL = "\"ValorCompra\":\"NIO: ";
        private static final String SELL_LITERAL = "\"ValorVenta\":\"USD: ";
        private static final String CLOSE_LITERAL = "\",";

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
        public String fetchAsPlainText() {
            try {
                return Jsoup
                        .connect(url())
                        .validateTLSCertificates(false)
                        .cookies(ExecutionContext.getInstance().cookies(bank()))
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
            return extractDataFromPlainTextResponse(BUY_LITERAL, CLOSE_LITERAL, SELL_LITERAL, CLOSE_LITERAL, OFFSET_TEXT);
        }

    };

    private static final int bankCount = CommercialBankExchangeRateScraperType.values().length;
    private static final List<CommercialBank> commercialBanks = new ArrayList<>(bankCount);

    static {
        for (CommercialBankExchangeRateScraper bankScraper : CommercialBankExchangeRateScraperType.values()) {
            commercialBanks.add(new CommercialBank(bankScraper.bank(), bankScraper.description(), bankScraper.url()));
        }
    }

    private final String description;
    private final String url;

    CommercialBankExchangeRateScraperType(String description, String url) {
        this.description = description;
        this.url = url;
    }

    CommercialBankExchangeRateScraperType(String url) {
        this.description = name();
        this.url = url;
    }

    @Override
    public String bank() {
        return name();
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public String url() {
        return url;
    }

    public static int bankCount() {
        return bankCount;
    }

    public static List<CommercialBank> commercialBanks() {
        return commercialBanks;
    }

}
