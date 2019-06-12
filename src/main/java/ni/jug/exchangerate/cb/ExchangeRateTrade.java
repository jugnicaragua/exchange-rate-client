package ni.jug.exchangerate.cb;

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
    private final boolean bestBuyPrice;
    private final boolean bestSellPrice;
    private final boolean worstBuyPrice;
    private final boolean worstSellPrice;

    public ExchangeRateTrade(String bank, LocalDate date, BigDecimal buy, BigDecimal sell, BigDecimal bestBuyPrice,
            BigDecimal bestSellPrice, BigDecimal worstBuyPrice, BigDecimal worstSellPrice) {
        this.bank = Objects.requireNonNull(bank);
        this.date = Objects.requireNonNull(date);
        this.buy = Objects.requireNonNull(buy);
        this.sell = Objects.requireNonNull(sell);
        this.bestBuyPrice = bestBuyPrice != null && bestBuyPrice.compareTo(buy) == 0;
        this.bestSellPrice = bestSellPrice != null && bestSellPrice.compareTo(sell) == 0;
        this.worstBuyPrice = worstBuyPrice != null && worstBuyPrice.compareTo(buy) == 0;
        this.worstSellPrice = worstSellPrice != null && worstSellPrice.compareTo(sell) == 0;
    }

    public ExchangeRateTrade(String bank, BigDecimal buy, BigDecimal sell) {
        this(bank, LocalDate.now(), buy, sell, null, null, null, null);
    }

    public String bank() {
        return bank;
    }

    public LocalDate date() {
        return date;
    }

    public BigDecimal buy() {
        return buy;
    }

    public BigDecimal sell() {
        return sell;
    }

    public boolean isBestBuyPrice() {
        return bestBuyPrice;
    }

    public boolean isBestSellPrice() {
        return bestSellPrice;
    }

    public boolean isWorstBuyPrice() {
        return worstBuyPrice;
    }

    public boolean isWorstSellPrice() {
        return worstSellPrice;
    }

    public ExchangeRateTrade usingPrices(BigDecimal bestBuyPrice,
            BigDecimal bestSellPrice, BigDecimal worstBuyPrice, BigDecimal worstSellPrice) {
        return new ExchangeRateTrade(bank, date, buy, sell, bestBuyPrice, bestSellPrice, worstBuyPrice, worstSellPrice);
    }

    public boolean isDataFetched() {
        return !(BigDecimal.ZERO.compareTo(buy) == 0 || BigDecimal.ZERO.compareTo(sell) == 0);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.bank);
        hash = 31 * hash + Objects.hashCode(this.date);
        hash = 31 * hash + Objects.hashCode(this.buy);
        hash = 31 * hash + Objects.hashCode(this.sell);
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
        if (!Objects.equals(this.buy, other.buy)) {
            return false;
        }
        if (!Objects.equals(this.sell, other.sell)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ExchangeRateTrade{" + "bank=" + bank + ", date=" + date + ", buy=" + buy + ", sell=" + sell + ", bestBuyPrice=" +
                bestBuyPrice + ", bestSellPrice=" + bestSellPrice + ", worstBuyPrice=" + worstBuyPrice + ", worstSellPrice=" +
                worstSellPrice + '}';
    }

}
