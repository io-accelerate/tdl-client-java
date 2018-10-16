package tdl.client.queue;

import tdl.client.audit.AuditStream;
import tdl.client.audit.StdoutAuditStream;

public class ImplementationRunnerConfig {
    private String hostname;
    private int port;
    private AuditStream auditStream;
    private int requestTimeoutMillis;
    private String requestQueueName;
    private String responseQueueName;

    public ImplementationRunnerConfig() {
        port = 61616;
        requestTimeoutMillis = 500;
        auditStream = new StdoutAuditStream();
    }

    public ImplementationRunnerConfig setHostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    public ImplementationRunnerConfig setPort(int port) {
        this.port = port;
        return this;
    }

    public ImplementationRunnerConfig setRequestQueueName(String requestQueueName) {
        this.requestQueueName = requestQueueName;
        return this;
    }

    public ImplementationRunnerConfig setResponseQueueName(String responseQueueName) {
        this.responseQueueName = responseQueueName;
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

    public String getRequestQueueName() {
        return requestQueueName;
    }

    public String getResponseQueueName() {
        return responseQueueName;
    }

    public int getRequestTimeoutMillis() {
        return requestTimeoutMillis;
    }
}
