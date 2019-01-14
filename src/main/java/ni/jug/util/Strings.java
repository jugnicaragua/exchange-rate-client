package ni.jug.util;

/**
 *
 * @author Armando Alaniz
 * @version 1.0
 * @since 1.0
 */
public final class Strings {

    public static final String EMPTY = "";

    private Strings() {
    }

    public static String substringBetween(String value, String left, String right, String fromText) {
        int start = -1, end = -1;
        int offset = -1;

        if (!(fromText == null || fromText.isEmpty())) {
            offset = value.indexOf(fromText);
            if (offset == -1) {
                return EMPTY;
            }
        }
        start = offset == -1 ? value.indexOf(left) : value.indexOf(left, offset + fromText.length());
        if (start == -1) {
            return EMPTY;
        }
        start += left.length();
        end = value.indexOf(right, start);
        if (end == -1) {
            return EMPTY;
        }

        return value.substring(start, end);
    }

    public static String substringBetween(String value, String left, String right) {
        return Strings.substringBetween(value, left, right, null);
    }

    public static String substringAfter(String value, String fromText) {
        int offset = value.indexOf(fromText);
        if (offset == -1) {
            return EMPTY;
        }
        return value.substring(offset + fromText.length());
    }
}
