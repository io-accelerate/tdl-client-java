package tdl.client.audit;

import java.io.PrintStream;

/**
 * Created by julianghionoiu on 24/10/2015.
 */
public class StdoutAuditStream implements AuditStream {

    private final PrintStream printStream;

    public StdoutAuditStream() {
        printStream = System.out;
    }

    @Override
    public void println(String s) {
        printStream.println(s);
    }
}
