package ni.jug.cb.exchangerate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 *
 * @author Armando Alaniz
 * @version 1.0
 * @since 1.0
 */
public final class ExchangeRateTrade {

    private final String bank;
    private final LocalDate date;
    private final BigDecimal buy;
    private final BigDecimal sell;

    public ExchangeRateTrade(String bank, LocalDate date, BigDecimal buy, BigDecimal sell) {
        this.bank = bank;
        this.date = date;
        this.buy = buy;
        this.sell = sell;
    }

    public ExchangeRateTrade(String bank, BigDecimal buy, BigDecimal sell) {
        this.bank = bank;
        this.buy = buy;
        this.sell = sell;
        this.date = LocalDate.now();
    }

    public ExchangeRateTrade(String bank) {
        this.bank = bank;
        this.buy = BigDecimal.ZERO;
        this.sell = BigDecimal.ZERO;
        this.date = LocalDate.now();
    }

    public String getBank() {
        return bank;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getBuy() {
        return buy;
    }

    public BigDecimal getSell() {
        return sell;
    }

    public boolean isDataFetched() {
        return !(BigDecimal.ZERO.compareTo(buy) == 0 || BigDecimal.ZERO.compareTo(sell) == 0);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.bank);
        hash = 31 * hash + Objects.hashCode(this.date);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExchangeRateTrade other = (ExchangeRateTrade) obj;
        if (!Objects.equals(this.bank, other.bank)) {
            return false;
        }
        if (!Objects.equals(this.date, other.date)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ExchangeRateTrade{" + "bank=" + bank + ", date=" + date + ", buy=" + buy + ", sell=" + sell + '}';
    }

}
