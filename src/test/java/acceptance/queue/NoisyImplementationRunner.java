package acceptance.queue;

import io.accelerate.client.audit.AuditStream;
import io.accelerate.client.queue.ImplementationRunner;

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
