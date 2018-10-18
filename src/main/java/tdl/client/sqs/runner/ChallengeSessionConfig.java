package tdl.client.sqs.runner;

import tdl.client.audit.AuditStream;
import tdl.client.audit.StdoutAuditStream;

public class ChallengeSessionConfig {
    private AuditStream auditStream;
    private final String journeyId;
    private boolean recordingSystemShouldBeOn;
    private String hostname;
    private int port;
    private boolean useColours;


    public static ChallengeSessionConfig forJourneyId(String journeyId) {
        return new ChallengeSessionConfig(journeyId);
    }

    private ChallengeSessionConfig(String journeyId) {
        this.port = 8222;
        this.useColours = true;
        this.recordingSystemShouldBeOn = true;
        this.auditStream = new StdoutAuditStream();
        this.journeyId = journeyId;
    }

    public ChallengeSessionConfig withServerHostname(@SuppressWarnings("SameParameterValue") String hostname) {
        this.hostname = hostname;
        return this;
    }

    public ChallengeSessionConfig withPort(int port) {
        this.port = port;
        return this;
    }

    public ChallengeSessionConfig withColours(boolean useColours) {
        this.useColours = useColours;
        return this;
    }

    public ChallengeSessionConfig withAuditStream(AuditStream auditStream) {
        this.auditStream = auditStream;
        return this;
    }

    public ChallengeSessionConfig withRecordingSystemShouldBeOn(boolean recordingSystemShouldBeOn) {
        this.recordingSystemShouldBeOn = recordingSystemShouldBeOn;
        return this;
    }

    boolean getRecordingSystemShouldBeOn() {
        return recordingSystemShouldBeOn;
    }

    String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    String getJourneyId() {
        return journeyId;
    }

    AuditStream getAuditStream() {
        return auditStream;
    }

    boolean getUseColours() {
        return useColours;
    }
}
