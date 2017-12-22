package tdl.client.runner;

import tdl.client.audit.AuditStream;
import tdl.client.audit.StdoutAuditStream;

public class ChallengeSessionConfig {
    private AuditStream auditStream;
    private boolean recordingSystemShouldBeOn;
    private String hostname;
    private int port;
    private String journeyId;
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

    public boolean getRecordingSystemShouldBeOn() {
        return recordingSystemShouldBeOn;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public String getJourneyId() {
        return journeyId;
    }

    public AuditStream getAuditStream() {
        return auditStream;
    }

    public boolean getUseColours() {
        return useColours;
    }
}
