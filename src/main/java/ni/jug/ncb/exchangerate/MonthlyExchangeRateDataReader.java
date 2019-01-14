package ni.jug.ncb.exchangerate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 *
 * @author Armando Alaniz
 * @version 1.0
 * @since 2.0
 */
public interface MonthlyExchangeRateDataReader {

    Map<LocalDate, BigDecimal> processResult();

}
