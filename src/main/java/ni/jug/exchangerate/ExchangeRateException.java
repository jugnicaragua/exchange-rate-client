package ni.jug.exchangerate;

/**
 *
 * @author aalaniz
 * @version 1.0
 * @since 3.0
 */
public class ExchangeRateException extends Exception {

    public ExchangeRateException(String message) {
        super(message);
    }

    public ExchangeRateException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExchangeRateException(String msg, Object... args) {
        this(null, msg, args);
    }

    public ExchangeRateException(Throwable cause, String msg, Object... args) {
        super(String.format(msg, args));
    }
}
