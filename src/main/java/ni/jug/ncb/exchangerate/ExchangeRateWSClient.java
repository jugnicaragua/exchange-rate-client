package ni.jug.ncb.exchangerate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Objects;
import ni.jug.ncb.exchangerate.ws.RecuperaTCMesResponse;
import ni.jug.ncb.exchangerate.ws.TipoCambioBCN;
import ni.jug.ncb.exchangerate.ws.TipoCambioBCNSoap;

/**
 *
 * @author Armando Alaniz
 * @version 2.0
 * @since 1.0
 */
public class ExchangeRateWSClient implements ExchangeRateClient {

    private TipoCambioBCNSoap getPort() {
        return new TipoCambioBCN().getTipoCambioBCNSoap();
    }

    @Override
    public BigDecimal getExchangeRate(LocalDate date) {
        Objects.requireNonNull(date);
        doValidateYear(date);
        double taxExchange = getPort().recuperaTCDia(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        return new BigDecimal(String.valueOf(taxExchange));
    }

    @Override
    public MonthlyExchangeRate getMonthlyExchangeRate(int year, Month month) {
        Objects.requireNonNull(month);
        doValidateYear(year, month);
        RecuperaTCMesResponse.RecuperaTCMesResult wsData = getPort().recuperaTCMes(year, month.getValue());
        return new MonthlyExchangeRate(new MonthlyExchangeRateWSDataReader(wsData));
    }

}
