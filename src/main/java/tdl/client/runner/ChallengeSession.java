package tdl.client.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tdl.client.queue.ImplementationRunner;


public class ChallengeSession {
    private final Logger LOG = LoggerFactory.getLogger(ChallengeSession.class);
    private String hostname;
    private int port;
    private String journeyId;
    private boolean useColours;
    private final String username;
    private ImplementationRunner implementationRunner;
    private IConsoleOut consoleOut;
    private RecordingSystem recordingSystem;
    private ChallengeServerClient challengeServerClient;
    private ActionProvider userInputCallback;

    public static ChallengeSession forUsername(@SuppressWarnings("SameParameterValue") String username) {
        return new ChallengeSession(username);
    }

    private ChallengeSession(String username) {
        this.username = username;
    }

    public ChallengeSession withServerHostname(@SuppressWarnings("SameParameterValue") String hostname) {
        this.hostname = hostname;
        return this;
    }

    public ChallengeSession withPort(int port) {
        this.port = port;
        return this;
    }

    public ChallengeSession withJourneyId(String journeyId) {
        this.journeyId = journeyId;
        return this;
    }

    public ChallengeSession withColours(boolean useColours) {
        this.useColours = useColours;
        return this;
    }

    public ChallengeSession withConsoleOut(IConsoleOut consoleOut) {
        this.consoleOut = consoleOut;
        return this;
    }

    public ChallengeSession withImplementationRunner(ImplementationRunner implementationRunner) {
        this.implementationRunner = implementationRunner;
        return this;
    }

    public ChallengeSession withRecordingSystemOn(boolean recordingSystemOn) {
        this.recordingSystem = new RecordingSystem(recordingSystemOn);
        return this;
    }

    public ChallengeSession withActionProvider(ActionProvider callback) {
        this.userInputCallback = callback;
        return this;
    }

    //~~~~~~~~ The entry point ~~~~~~~~~

    public void start() {
        if (!recordingSystem.isRecordingSystemOk()) {
            consoleOut.println("Please run `record_screen_and_upload` before continuing.");
            return;
        }
        consoleOut.println("Connecting to " + hostname);
        runApp();
    }

    private void runApp() {
        challengeServerClient = new ChallengeServerClient(hostname, port, journeyId, useColours);

        try {
            boolean shouldContinue = checkStatusOfChallenge();
            if (shouldContinue) {
                String userInput = this.userInputCallback.get();
                consoleOut.println("Selected action is: " + userInput);
                String roundDescription = executeUserAction(userInput);
                RoundManagement.saveDescription(recordingSystem, roundDescription, consoleOut);
            }
        }  catch (ChallengeServerClient.ServerErrorException e) {
            String msg = "Server experienced an error. Try again in a few minutes.";
            LOG.error(msg, e);
            consoleOut.println(msg);
        } catch (ChallengeServerClient.OtherCommunicationException e) {
            String msg = "Client threw an unexpected error. Try again.";
            LOG.error(msg, e);
            consoleOut.println(msg);
        } catch (ChallengeServerClient.ClientErrorException e) {
            LOG.error("The client sent something the server didn't expect.");
            consoleOut.println(e.getResponseMessage());
        }
    }

    private boolean checkStatusOfChallenge() throws ChallengeServerClient.ServerErrorException, ChallengeServerClient.OtherCommunicationException, ChallengeServerClient.ClientErrorException {
        String journeyProgress = challengeServerClient.getJourneyProgress();
        consoleOut.println(journeyProgress);

        String availableActions = challengeServerClient.getAvailableActions();
        consoleOut.println(availableActions);

        return !availableActions.contains("No actions available.");
    }

    private String executeUserAction(String userInput) throws ChallengeServerClient.ServerErrorException, ChallengeServerClient.OtherCommunicationException, ChallengeServerClient.ClientErrorException {
        if (userInput.equals("deploy")) {
            implementationRunner.run();
            String lastFetchedRound = RoundManagement.getLastFetchedRound();
            recordingSystem.deployNotifyEvent(lastFetchedRound);
        }
        return executeAction(userInput);
    }

    private String executeAction(String userInput) throws ChallengeServerClient.ServerErrorException, ChallengeServerClient.OtherCommunicationException, ChallengeServerClient.ClientErrorException {
        String actionFeedback = challengeServerClient.sendAction(userInput);
        consoleOut.println(actionFeedback);
        return challengeServerClient.getRoundDescription();
    }
}
