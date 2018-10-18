package acceptance.jms.runner;

import audit.AuditStream;

public class TestAuditStream implements AuditStream {
    private String total = "";

    @Override
    public void println(String s) {
        total += s + "\n";
    }

    String getTotal() {
        return total;
    }
}
