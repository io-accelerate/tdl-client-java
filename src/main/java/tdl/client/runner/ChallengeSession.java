package tdl.client.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;


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

    //~~~~~~~~ The entry point ~~~~~~~~~

    public void start(String[] args) {
        if (!RecordingSystem.isRecordingSystemOk()) {
            consoleOut.println("Please run `record_screen_and_upload` before continuing.");
            return;
        }
        consoleOut.println("Connecting to " + hostname);
        runApp(args);
    }

    private void runApp(String[] args) {
        CombinedClient combinedClient = new CombinedClient(journeyId, useColours, hostname, port, consoleOut, implementationRunner);

        try {
            boolean shouldContinue = combinedClient.checkStatusOfChallenge();
            if (shouldContinue) {
                String userInput = getUserInput(args);
                String roundDescription = combinedClient.executeUserAction(userInput);
                RoundManagement.saveDescription(roundDescription, consoleOut);
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
        return args.length > 0 ? args[0] : readInputFromConsole();
    }

    private String readInputFromConsole() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            LOG.error("Could not read user input.", e);
            return "error";
        }
    }
}
