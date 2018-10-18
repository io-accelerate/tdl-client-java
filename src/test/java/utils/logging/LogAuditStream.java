package utils.logging;

import audit.AuditStream;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import static java.lang.System.getProperty;

/**
 * Created by julianghionoiu on 11/10/2015.
 */
public class LogAuditStream implements AuditStream {
    private static final ByteArrayOutputStream LOG
            = new ByteArrayOutputStream();
    private final PrintStream logStream;
    private final AuditStream originalStream;

    public LogAuditStream(AuditStream originalStream) {
        logStream = new PrintStream(LOG);
        this.originalStream = originalStream;
        LOG.reset();
    }

    //~~~~ Log control methods

    public void clearLog() {
        LOG.reset();
    }

    public String getLog() {
        String encoding = getProperty("file.encoding");
        try {
            return LOG.toString(encoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    //~~~ Forward println

    @Override
    public void println(String s) {
        logStream.println(s);
        originalStream.println(s);
    }
}
