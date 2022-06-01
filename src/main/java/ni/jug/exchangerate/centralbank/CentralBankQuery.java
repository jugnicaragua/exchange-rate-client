package ni.jug.exchangerate.centralbank;

import ni.jug.exchangerate.ExchangeRateException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * @author aalaniz
 */
public interface CentralBankQuery {

    MonthlyExchangeRate getMonthlyExchangeRate(YearMonth period) throws ExchangeRateException;

    BigDecimal getExchangeRate(LocalDate date) throws ExchangeRateException;
}
