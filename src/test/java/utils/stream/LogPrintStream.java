package utils.stream;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import static java.lang.System.getProperty;

/**
 * Created by julianghionoiu on 11/10/2015.
 */
public class LogPrintStream extends PrintStream {
    private static final ByteArrayOutputStream LOG
            = new ByteArrayOutputStream();
    private final PrintStream originalStream;

    public LogPrintStream(PrintStream originalStream) {
        super(LOG);
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
    public void println() {
        super.println();
        originalStream.println();
    }


    @Override
    public void println(String s) {
        super.println(s);
        originalStream.println(s);
    }
}
