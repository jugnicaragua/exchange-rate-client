package ni.jug.exchangerate;

/**
 * @author aalaniz
 */
public class ExchangeRateException extends Exception {

    public ExchangeRateException(String message) {
        super(message);
    }

    public ExchangeRateException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExchangeRateException(String message, Object... args) {
        this(message, null, args);
    }

    public ExchangeRateException(String message, Throwable cause, Object... args) {
        super(String.format(message, args), cause);
    }
}
