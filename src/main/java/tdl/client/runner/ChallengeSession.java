package tdl.client.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;


public class ChallengeSession {
    private final Logger LOG = LoggerFactory.getLogger(ChallengeSession.class);
    private String hostname;
    private int port;
    private String journeyId;
    private boolean useColours;
    private final String username;
    private BufferedReader reader;
    private ImplementationRunner implementationRunner;
    private IConsoleOut consoleOut;
    private RecordingSystem recordingSystem;
    private HttpClient httpClient;

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

    public ChallengeSession withBufferedReader(BufferedReader reader) {
        this.reader = reader;
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

    //~~~~~~~~ The entry point ~~~~~~~~~

    public void start(String[] args) {
        if (!recordingSystem.isRecordingSystemOk()) {
            consoleOut.println("Please run `record_screen_and_upload` before continuing.");
            return;
        }
        consoleOut.println("Connecting to " + hostname);
        runApp(args);
    }

    private void runApp(String[] args) {
        httpClient = new HttpClient(hostname, port, journeyId, useColours);

        try {
            boolean shouldContinue = checkStatusOfChallenge();
            if (shouldContinue) {
                String userInput = getUserInput(args);
                consoleOut.println("Selected action is: " + userInput);
                String roundDescription = executeUserAction(userInput);
                RoundManagement.saveDescription(recordingSystem, roundDescription, consoleOut);
            }
        }  catch (HttpClient.ServerErrorException e) {
            LOG.error("Server experienced an error. Try again.", e);
        } catch (HttpClient.OtherCommunicationException e) {
            LOG.error("Client threw an unexpected error.", e);
        } catch (HttpClient.ClientErrorException e) {
            LOG.error("The client sent something the server didn't expect.");
            consoleOut.println(e.getResponseMessage());
        }
    }

    private String getUserInput(String[] args) {
        return args.length > 0 ? args[0] : "";
    }

    private boolean checkStatusOfChallenge() throws HttpClient.ServerErrorException, HttpClient.OtherCommunicationException, HttpClient.ClientErrorException {
        String journeyProgress = httpClient.getJourneyProgress();
        consoleOut.println(journeyProgress);

        String availableActions = httpClient.getAvailableActions();
        consoleOut.println(availableActions);

        return !availableActions.contains("No actions available.");
    }

    private String executeUserAction(String userInput) throws HttpClient.ServerErrorException, HttpClient.OtherCommunicationException, HttpClient.ClientErrorException {
        if (userInput.equals("deploy")) {
            implementationRunner.run();
            String lastFetchedRound = RoundManagement.getLastFetchedRound();
            recordingSystem.deployNotifyEvent(lastFetchedRound);
        }
        return executeAction(userInput);
    }

    private String executeAction(String userInput) throws HttpClient.ServerErrorException, HttpClient.OtherCommunicationException, HttpClient.ClientErrorException {
        String actionFeedback = httpClient.sendAction(userInput);
        consoleOut.println(actionFeedback);
        return httpClient.getRoundDescription();
    }
}
