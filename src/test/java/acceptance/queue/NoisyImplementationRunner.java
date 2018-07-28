package acceptance.queue;

import tdl.client.audit.AuditStream;
import tdl.client.queue.ImplementationRunner;

public class NoisyImplementationRunner implements ImplementationRunner {
    private final String deployMessage;
    private final AuditStream auditStream;

    public NoisyImplementationRunner(String deployMessage, AuditStream auditStream) {
        this.deployMessage = deployMessage;
        this.auditStream = auditStream;
    }
    @Override
    public void run() {
        auditStream.println(deployMessage);
    }
}
