package tdl.client.audit;

/**
 * Created by julianghionoiu on 24/10/2015.
 */
public interface AuditStream {
    void println();
    void println(String text);
    void printf(String format, Object... args);
}
