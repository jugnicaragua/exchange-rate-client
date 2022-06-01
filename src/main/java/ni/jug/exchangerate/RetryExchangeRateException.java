package ni.jug.exchangerate;

/**
 * @author aalaniz
 */
public class RetryExchangeRateException extends ExchangeRateException {

    public RetryExchangeRateException(String message) {
        super(message);
    }

    public RetryExchangeRateException(String message, Throwable cause) {
        super(message, cause);
    }

    public RetryExchangeRateException(String message, Object... args) {
        super(message, null, args);
    }

    public RetryExchangeRateException(String message, Throwable cause, Object... args) {
        super(message, cause, args);
    }
}
