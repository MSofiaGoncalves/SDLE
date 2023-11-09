package server;



import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LoggerFormatter extends Formatter{

    public String format(LogRecord record) {
        return String.format("%tH:%<tM:%<tS :: %s\n", record.getMillis(), record.getMessage());
    }

}