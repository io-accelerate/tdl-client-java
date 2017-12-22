package tdl.client.queue;

import tdl.client.audit.AuditStream;
import tdl.client.audit.StdoutAuditStream;

public class ImplementationRunnerConfig {
    private String hostname;
    private int port;
    private String uniqueId;
    private AuditStream auditStream;
    private int requestTimeoutMillis;

    public ImplementationRunnerConfig() {
        port = 61616;
        requestTimeoutMillis = 500;
        auditStream = new StdoutAuditStream();
    }

    public ImplementationRunnerConfig setHostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    public ImplementationRunnerConfig setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
        return this;
    }

    public ImplementationRunnerConfig setPort(int port) {
        this.port = port;
        return this;
    }

    public ImplementationRunnerConfig setAuditStream(AuditStream auditStream) {
        this.auditStream = auditStream;
        return this;
    }

    public ImplementationRunnerConfig setRequestTimeoutMillis(int requestTimeoutMillis) {
        this.requestTimeoutMillis = requestTimeoutMillis;
        return this;
    }

    public AuditStream getAuditStream() {
        return auditStream;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public int getRequestTimeoutMillis() {
        return requestTimeoutMillis;
    }
}
