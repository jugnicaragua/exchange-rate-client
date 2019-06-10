package ni.jug.util;

import java.util.Objects;

/**
 *
 * @author Armando Alaniz
 * @version 1.0
 * @since 1.0
 */
public final class Strings {

    public static final String EMPTY = "";
    public static final String COMMA = ",";
    public static final String HYPHEN = "-";
    public static final String DOUBLE_HYPHEN = "--";
    public static final String COLON = ":";
    public static final String EQUAL = "=";

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
        offset += fromText.length();
        if (offset >= value.length()) {
            return EMPTY;
        }
        return value.substring(offset);
    }

    public static boolean containsComma(String value) {
        return value.contains(COMMA);
    }

    public static boolean containsColon(String value) {
        return value.contains(COLON);
    }

    public static String[] splitCSV(String csv) {
        return csv.split("\\,");
    }

    public static String[] splitCSVAndGetFirst2Elements(String value) {
        String[] result = new String[2];
        int pos = value.indexOf(COLON);
        result[0] = value.substring(0, pos);
        if (pos < value.length() - 1) {
            result[1] = value.substring(pos + 1);
        }
        return result;
    }

}
