package ni.jug.util;

/**
 * @author aalaniz
 */
public final class Inputs {

    public static final String ERROR_NUMBER_OUT_OF_BOUNDS = "%d se encuentra fuera de rango [%d, %d].";

    private Inputs() {
    }

    public static int numberInRange(int value, int min, int max) {
        if (value < min || value > max) {
            String msg = String.format(ERROR_NUMBER_OUT_OF_BOUNDS, value, min, max);
            throw new IllegalArgumentException(msg);
        }
        return value;
    }
}
