package ni.jug.exchangerate.cli;

import ni.jug.exchangerate.ExchangeRateClient;
import ni.jug.exchangerate.ExchangeRateException;
import ni.jug.exchangerate.bank.BankExchangeRateSummary;
import ni.jug.exchangerate.bank.ExchangeRateTrade;
import ni.jug.exchangerate.centralbank.MonthlyExchangeRate;
import ni.jug.util.Dates;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author aalaniz
 */
public class ExchangeRateCLI {

    private static final Logger LOGGER = Logger.getLogger(ExchangeRateCLI.class.getName());

    private static final String COMMA = ",";
    private static final String SPACE = " ";
    private static final String PROMPT = "--> ";
    private static final String DASH_PROMPT = "------------------------------------------------------------------------\n";

    private static final String OPT_QUERY_BY_DATE = "-date";
    private static final String OPT_QUERY_BY_YEAR_MONTH = "-ym";
    private static final String OPT_COMMERCIAL_BANK = "-bank";
    private static final String OPT_HELP = "--help";

    private static final StringBuilder help = new StringBuilder();
    static {
        help.append("Opciones disponibles:\n");
        help.append("  -date: se puede consultar por una fecha, lista de fecha o rango de fechas. ");
        help.append("Por ejemplo: -date=[fecha], -date=[fecha1]:[fecha2], -date=[fecha1],[fecha2],...\n");
        help.append("  -ym: se puede consultar por año-mes. ");
        help.append("Por ejemplo: -ym=[año]-[mes], -ym=[año1]-[mes1]:[año2]-[mes2], -ym=[año1]-[mes1],[año2]-[mes2],...\n");
        help.append("  -bank: muestra el detalle de la venta y compra del dolar en los bancos comerciales\n");
    }

    private String messageForWrongDate(String strDate) {
        return "El valor [" + strDate + "] no es una fecha. Ingrese una fecha en formato ISO";
    }

    private void doAppendExchangeRateByDate(LocalDate date, BigDecimal exchangeRate, StringBuilder sb) {
        try {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(PROMPT).append(date).append(COMMA).append(SPACE).append(exchangeRate);
        } catch (IllegalArgumentException iae) {
            LOGGER.log(Level.SEVERE, iae.getMessage());
        }
    }

    private void queryBySpecificDates(String value) throws ExchangeRateException {
        LOGGER.info("Obtener tasa de cambio por fecha");
        BigDecimal exchangeRate;

        StringBuilder result = new StringBuilder(SPACE);
        CLIHelper.OptionListValue optionListValue = new CLIHelper.OptionListValue(value);
        for (int i = 0; i < optionListValue.getSize(); i++) {
            Object obj = optionListValue.getValues()[i];

            if (obj instanceof String) {
                String strDate = (String) obj;

                try {
                    LocalDate date = Dates.toLocalDate(strDate);
                    exchangeRate = ExchangeRateClient.INSTANCE
                            .centralBankQuery()
                            .getExchangeRate(date);

                    doAppendExchangeRateByDate(date, exchangeRate, result);
                } catch (DateTimeParseException dtpe) {
                    LOGGER.log(Level.SEVERE, messageForWrongDate(strDate));
                }
            } else if (obj instanceof CLIHelper.OptionRangeValue) {
                CLIHelper.OptionRangeValue range = (CLIHelper.OptionRangeValue) obj;

                try {
                    LocalDate date1 = Dates.toLocalDate(range.getFrom());
                    LocalDate date2 = range.getTo() == null ? Dates.getLastDateOfMonthOf(date1) : Dates.toLocalDate(range.getTo());

                    Dates.validateDateRange(date1, date2);

                    while (date1.compareTo(date2) <= 0) {
                        exchangeRate = ExchangeRateClient.INSTANCE
                                .centralBankQuery()
                                .getExchangeRate(date1);
                        doAppendExchangeRateByDate(date1, exchangeRate, result);
                        date1 = date1.plusDays(1);
                    }
                } catch (DateTimeParseException dtpe) {
                    LOGGER.log(Level.SEVERE, "No se pudo extraer el rango de fechas del valor [{0}]", range.getRaw());
                } catch (IllegalArgumentException iae) {
                    LOGGER.log(Level.SEVERE, iae.getMessage());
                }
            } else {
                throw new IllegalStateException("Tipo de dato no reconocido");
            }
        }

        if (result.length() > 0) {
            LOGGER.info(result.toString());
        }
    }

    private void doAppendMonthlyExchangeRate(MonthlyExchangeRate monthlyExchangeRate, StringBuilder sb) {
        if (sb.length() > 0) {
            sb.append("\n");
        }
        for (Map.Entry<LocalDate, BigDecimal> exchangeRateByDate : monthlyExchangeRate) {
            sb.append(PROMPT);
            sb.append(exchangeRateByDate.getKey());
            sb.append(COMMA);
            sb.append(SPACE);
            sb.append(exchangeRateByDate.getValue());
            sb.append("\n");
        }
    }

    private void queryBySpecificYearMonths(String value) throws ExchangeRateException {
        LOGGER.info("Obtener tasa de cambio por año-mes");
        MonthlyExchangeRate monthlyExchangeRate;

        StringBuilder result = new StringBuilder(SPACE);
        CLIHelper.OptionListValue optionListValue = new CLIHelper.OptionListValue(value);
        for (int i = 0; i < optionListValue.getSize(); i++) {
            Object obj = optionListValue.getValues()[i];

            if (obj instanceof String) {
                String yearMonth = (String) obj;

                try {
                    YearMonth period = YearMonth.parse(yearMonth);
                    monthlyExchangeRate = ExchangeRateClient.INSTANCE
                            .centralBankQuery()
                            .getMonthlyExchangeRate(period);

                    doAppendMonthlyExchangeRate(monthlyExchangeRate, result);
                } catch (DateTimeParseException dtpe) {
                    LOGGER.log(Level.SEVERE, messageForWrongDate(yearMonth));
                }
            } else if (obj instanceof CLIHelper.OptionRangeValue) {
                CLIHelper.OptionRangeValue range = (CLIHelper.OptionRangeValue) obj;

                try {
                    LocalDate date1 = Dates.toFirstDateOfYearMonth(range.getFrom());
                    LocalDate date2 = range.getTo() == null ? Dates.getCurrentDateOrLastDayOf(date1) :
                            Dates.toFirstDateOfYearMonth(range.getTo());

                    Dates.validateDateRange(date1, date2);

                    while (date1.compareTo(date2) <= 0) {
                        monthlyExchangeRate = ExchangeRateClient.INSTANCE
                                .centralBankQuery()
                                .getMonthlyExchangeRate(YearMonth.from(date1));
                        doAppendMonthlyExchangeRate(monthlyExchangeRate, result);
                        date1 = date1.plusMonths(1);
                    }
                } catch (DateTimeParseException dtpe) {
                    LOGGER.log(Level.SEVERE, "No se pudo extraer el rango de fechas del valor [{0}]", range.getRaw());
                } catch (IllegalArgumentException iae) {
                    LOGGER.log(Level.SEVERE, iae.getMessage());
                }
            } else {
                throw new IllegalStateException("Tipo de dato no reconocido");
            }
        }

        if (result.length() > 0) {
            LOGGER.info(result.toString());
        }
    }

    private void fetchExchangeRateFromCommercialBanks() throws ExchangeRateException {
        BankExchangeRateSummary bankExchangeRateSummary = ExchangeRateClient.INSTANCE
                .bankQuery()
                .bankSummary();
        BigDecimal officialExchangeRate = ExchangeRateClient.INSTANCE
                .centralBankQuery()
                .getExchangeRate(LocalDate.now());

        StringBuilder result = new StringBuilder("\n");
        result.append(DASH_PROMPT);
        result.append("Bancos no disponibles: ");
        result.append(bankExchangeRateSummary.unavailableBanks().stream().collect(Collectors.joining(", ")));
        result.append("\n");
        result.append(DASH_PROMPT);
        result.append("\n");
        result.append(DASH_PROMPT);
        result.append(String.format("%-15s", "Banco"));
        result.append(String.format("%12s", "Venta"));
        result.append(String.format("%12s", "Compra"));
        result.append(String.format("%12s", "Oficial"));
        result.append("\n");
        result.append(DASH_PROMPT);
        for (ExchangeRateTrade trade : bankExchangeRateSummary) {
            result.append(String.format("%-15s", trade.bank()));
            String sell = trade.sell().toPlainString() + (trade.isBestSellPrice() ? "*" : "");
            result.append(String.format("%12s", sell));
            String buy = trade.buy().toPlainString() + (trade.isBestBuyPrice() ? "*" : "");
            result.append(String.format("%12s", buy));
            result.append(String.format("%12s", officialExchangeRate.toPlainString()));
            result.append("\n");
        }
        result.append("\n* Mejor opcion");

        LOGGER.info(result.toString());
    }

    public void handleRequest(String[] args) throws ExchangeRateException {
        if (args.length == 0) {
            throw new IllegalArgumentException("Se requiere al menos un argumento");
        }
        CLIHelper.validateOptions(args, OPT_QUERY_BY_DATE, OPT_QUERY_BY_YEAR_MONTH, OPT_COMMERCIAL_BANK, OPT_HELP);

        // Extraer primero los valores para disparar validaciones
        String queryByDate = CLIHelper.searchValueOf(OPT_QUERY_BY_DATE, args);
        String queryByYearMonth = CLIHelper.searchValueOf(OPT_QUERY_BY_YEAR_MONTH, args);

        if (!queryByDate.isEmpty()) {
            queryBySpecificDates(queryByDate);
        }
        if (!queryByYearMonth.isEmpty()) {
            queryBySpecificYearMonths(queryByYearMonth);
        }
        if (CLIHelper.checkOption(OPT_HELP, args)) {
            printUsage();
        }
        if (CLIHelper.checkOption(OPT_COMMERCIAL_BANK, args)) {
            fetchExchangeRateFromCommercialBanks();
        }
    }

    public static void printUsage() {
        LOGGER.info(help.toString());
    }

    public static void main(String[] args) {
        try {
            new ExchangeRateCLI().handleRequest(args);
        } catch (ExchangeRateException | IllegalArgumentException ex) {
            LOGGER.severe(ex.getMessage());
            printUsage();
        }
    }
}
