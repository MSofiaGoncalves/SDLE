package server;



import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class LoggerFormatter extends Formatter{

    // ANSI escape codes for colors
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_BLUE = "\u001B[34m";

    public String format(LogRecord record) {
        String color = determineColor(record.getLevel());
        return String.format("%s%tH:%<tM:%<tS.%<tL :: %s%s\n", color, record.getMillis(), record.getMessage(), ANSI_RESET);
    }

    private String determineColor(Level level) {
        if (level.intValue() >= Level.SEVERE.intValue()) {
            return ANSI_RED;
        } else if (level.intValue() >= Level.WARNING.intValue()) {
            return ANSI_YELLOW;
        } else {
            return ANSI_BLUE;
        }
    }
}