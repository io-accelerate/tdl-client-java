package tdl.client.audit;

import java.io.PrintStream;

/**
 * Created by julianghionoiu on 24/10/2015.
 */
public class StdoutAuditStream implements AuditStream {

    private PrintStream printStream;

    public StdoutAuditStream() {
        printStream = System.out;
    }

    @Override
    public void println() {
        printStream.println();
    }

    @Override
    public void println(String text) {
        printStream.println();
    }

    @Override
    public void printf(String format, Object... args) {
        printStream.printf(format, args);
    }
}
