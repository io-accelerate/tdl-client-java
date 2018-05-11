package tdl.client.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tdl.client.audit.AuditStream;
import tdl.client.queue.ImplementationRunner;


public class ChallengeSession {
    private final Logger LOG = LoggerFactory.getLogger(ChallengeSession.class);
    private ChallengeSessionConfig config;
    private ImplementationRunner implementationRunner;
    private RecordingSystem recordingSystem;
    private ChallengeServerClient challengeServerClient;
    private ActionProvider userInputCallback;

    public static ChallengeSession forRunner(ImplementationRunner implementationRunner) {
        return new ChallengeSession(implementationRunner);
    }

    private ChallengeSession(ImplementationRunner runner) {
        this.implementationRunner = runner;
    }

    public ChallengeSession withConfig(ChallengeSessionConfig config) {
        this.config = config;
        return this;
    }

    public ChallengeSession withActionProvider(ActionProvider callback) {
        this.userInputCallback = callback;
        return this;
    }

    //~~~~~~~~ The entry point ~~~~~~~~~

    public void start() {
        recordingSystem = new RecordingSystem(config.getRecordingSystemShouldBeOn());
        AuditStream auditStream = config.getAuditStream();

        if (!recordingSystem.isRecordingSystemOk()) {
            auditStream.println("Please run `record_screen_and_upload` before continuing.");
            return;
        }
        auditStream.println("Connecting to " + config.getHostname());
        runApp();
    }

    private void runApp() {
        AuditStream auditStream = config.getAuditStream();
        challengeServerClient = new ChallengeServerClient(config.getHostname(), config.getPort(), config.getJourneyId(), config.getUseColours());

        try {
            boolean shouldContinue = checkStatusOfChallenge();
            if (shouldContinue) {
                String userInput = this.userInputCallback.get();
                auditStream.println("Selected action is: " + userInput);
                String roundDescription = executeUserAction(userInput);
                RoundManagement.saveDescription(recordingSystem, roundDescription, auditStream);
            }
        }  catch (ChallengeServerClient.ServerErrorException e) {
            String msg = "Server experienced an error. Try again in a few minutes.";
            LOG.error(msg, e);
            auditStream.println(msg);
        } catch (ChallengeServerClient.OtherCommunicationException e) {
            String msg = "Client threw an unexpected error. Try again.";
            LOG.error(msg, e);
            auditStream.println(msg);
        } catch (ChallengeServerClient.ClientErrorException e) {
            LOG.error("The client sent something the server didn't expect.");
            auditStream.println(e.getResponseMessage());
        }
    }

    private boolean checkStatusOfChallenge() throws ChallengeServerClient.ServerErrorException, ChallengeServerClient.OtherCommunicationException, ChallengeServerClient.ClientErrorException {
        AuditStream auditStream = config.getAuditStream();

        String journeyProgress = challengeServerClient.getJourneyProgress();
        auditStream.println(journeyProgress);

        String availableActions = challengeServerClient.getAvailableActions();
        auditStream.println(availableActions);

        return !availableActions.contains("No actions available.");
    }

    private String executeUserAction(String userInput) throws ChallengeServerClient.ServerErrorException, ChallengeServerClient.OtherCommunicationException, ChallengeServerClient.ClientErrorException {
        if (userInput.equals("deploy")) {
            implementationRunner.run();
            String lastFetchedRound = RoundManagement.getLastFetchedRound();
            recordingSystem.notifyEvent(lastFetchedRound, RecordingSystem.Event.ROUND_SOLUTION_DEPLOY);
        }
        return executeAction(userInput);
    }

    private String executeAction(String userInput) throws ChallengeServerClient.ServerErrorException, ChallengeServerClient.OtherCommunicationException, ChallengeServerClient.ClientErrorException {
        String actionFeedback = challengeServerClient.sendAction(userInput);
        if (actionFeedback.contains("Round time for")) {
            String lastFetchedRound = RoundManagement.getLastFetchedRound();
            recordingSystem.notifyEvent(lastFetchedRound, RecordingSystem.Event.ROUND_COMPLETED);
        }
        config.getAuditStream().println(actionFeedback);
        return challengeServerClient.getRoundDescription();
    }
}
