package ni.jug.ncb.exchangerate.cli;

/**
 *
 * @author Armando Alaniz
 * @version 1.0
 * @since 1.0
 */
public final class CLIHelper {

    private static final String HYPHEN_STR = "-";
    private static final String DOUBLE_HYPHEN_STR = "--";
    private static final String EMPTY_STR = "";
    private static final String COMMA_STR = ",";
    private static final String COLON_STR = ":";

    private static final char COLON = ':';
    private static final char EQUAL = '=';

    private CLIHelper() {
    }

    private static boolean thereIsNoOptionIndicator(String namedArgument) {
        return !(namedArgument.startsWith(HYPHEN_STR) || namedArgument.startsWith(DOUBLE_HYPHEN_STR));
    }

    private static boolean thereIsNoAssignment(String argument) {
        return argument.indexOf(EQUAL) == -1;
    }

    private static String findOptionValueOf(String argument) {
        int pos = argument.indexOf(EQUAL);
        return pos == argument.length() - 1 ? EMPTY_STR : argument.substring(pos + 1);
    }

    private static void doValidateNamedArgument(String namedArgument) {
        if (thereIsNoOptionIndicator(namedArgument)) {
            throw new IllegalArgumentException("Para extraer el valor de una opción se debe usar guión o dos guiones en el "
                    + "nombre de la opción [" + namedArgument + "]");
        }
    }

    public static String findOptionValueOf(String namedArgument, String[] args) {
        doValidateNamedArgument(namedArgument);

        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith(namedArgument)) {
                if (thereIsNoAssignment(args[i])) {
                    throw new IllegalArgumentException("Error de sintaxis en parametro " + namedArgument + ", es necesario "
                            + "especificar un valor usando el signo igual (=)");
                }

                return findOptionValueOf(args[i]);
            }
        }

        return EMPTY_STR;
    }

    public static boolean optionIsPresent(String namedArgument, String[] args) {
        doValidateNamedArgument(namedArgument);

        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith(namedArgument)) {
                return true;
            }
        }
        return false;
    }

    private static String[] splitCommaSeparatedValue(String csv) {
        return csv.split("\\,");
    }

    private static boolean containsComma(String value) {
        return value.contains(COMMA_STR);
    }

    private static boolean containsColon(String value) {
        return value.contains(COLON_STR);
    }

    private static String[] extractTwoValues(String value, char delimiter) {
        String[] result = new String[2];
        int pos = value.indexOf(delimiter);
        result[0] = value.substring(0, pos);
        if (pos < value.length() - 1) {
            result[1] = value.substring(pos + 1);
        }
        return result;
    }

    private static String[] extractTwoValues(String value) {
        return extractTwoValues(value, COLON);
    }

    public static class OptionRangeValue {

        private final String raw;
        private final String from;
        private final String to;

        public OptionRangeValue(String raw) {
            String[] rangeValues = CLIHelper.extractTwoValues(raw);
            this.raw = raw;
            this.from = rangeValues[0];
            this.to = rangeValues[1];
        }

        public String getRaw() {
            return raw;
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }

    }

    public static class OptionListValue {

        private final String raw;
        private final Object[] values;
        private final int size;

        public OptionListValue(String raw) {
            this.raw = raw;
            this.values = process(raw);
            this.size = this.values.length;
        }

        private Object[] process(String raw) {
            Object[] result;

            if (CLIHelper.containsComma(raw) || CLIHelper.containsColon(raw)) {
                String[] values = CLIHelper.splitCommaSeparatedValue(raw);
                result = new Object[values.length];
                for (int i = 0; i < values.length; i++) {
                    String value = values[i];
                    if (CLIHelper.containsColon(value)) {
                        result[i] = new OptionRangeValue(value);
                    } else {
                        result[i] = value;
                    }
                }
            } else {
                result = new Object[1];
                result[0] = raw;
            }

            return result;
        }

        public String getRaw() {
            return raw;
        }

        public Object[] getValues() {
            return values;
        }

        public int getSize() {
            return size;
        }

    }

}
