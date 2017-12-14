package tdl.client.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tdl.client.ProcessingRules;
import tdl.client.abstractions.UserImplementation;
import tdl.client.actions.ClientAction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static tdl.client.actions.ClientActions.publish;


public class ClientRunner {
    private final Logger LOG = LoggerFactory.getLogger(ClientRunner.class);
    private String hostname;
    private int port;
    private String journeyId;
    private boolean useColours;
    private Runnable deployCallback;
    private boolean recordingSystemOk;
    private final String username;
    private final Map<String, UserImplementation> solutions;
    private ClientAction deployAction;
    private BufferedReader reader;
    private Consumer<String> notifyRecordSystemCallback;
    private PrintStream writer;

    public static ClientRunner forUsername(@SuppressWarnings("SameParameterValue") String username) {
        return new ClientRunner(username);
    }

    private ClientRunner(String username) {
        this.username = username;
        this.solutions = new HashMap<>();
    }

    public ClientRunner withServerHostname(@SuppressWarnings("SameParameterValue") String hostname) {
        this.hostname = hostname;
        return this;
    }

    public ClientRunner withPort(int port) {
        this.port = port;
        return this;
    }

    public ClientRunner withJourneyId(String journeyId) {
        this.journeyId = journeyId;
        return this;
    }

    public ClientRunner withColours(boolean useColours) {
        this.useColours = useColours;
        return this;
    }

    public ClientRunner withDeployCallback(Runnable deployCallback) {
        this.deployCallback = deployCallback;
        return this;
    }

    public ClientRunner withDeployAction(ClientAction deployAction) {
        this.deployAction = deployAction;
        return this;
    }

    public ClientRunner withBufferedReader(BufferedReader reader) {
        this.reader = reader;
        return this;
    }

    public ClientRunner withOutputStream(PrintStream out) {
        this.writer = out;
        return this;
    }

    public ClientRunner withRecordingSystemOk(boolean recordingSystemOk) {
        this.recordingSystemOk = recordingSystemOk;
        return this;
    }

    public ClientRunner withNotifyRecordSystemCallback(Consumer<String> notifyRecordSystemCallback) {
        this.notifyRecordSystemCallback = notifyRecordSystemCallback;
        return this;
    }

    public ClientRunner withSolutionFor(String methodName, UserImplementation solution) {
        solutions.put(methodName, solution);
        return this;
    }

    //~~~~~~~~ The entry point ~~~~~~~~~

    public void start(String[] args) {
        if (!this.recordingSystemOk) {
            writer.println("Please run `record_screen_and_upload` before continuing.");
            return;
        }
        writer.println("Connecting to " + hostname);
        runApp(args);
    }

    private void runApp(String[] args) {
        CombinedClient combinedClient = new CombinedClient(journeyId, useColours, hostname, port, username, writer);

        try {
            boolean shouldContinue = combinedClient.checkStatusOfChallenge();
            if (shouldContinue) {
                String userInput = getUserInput(args);
                ProcessingRules deployProcessingRules = createDeployProcessingRules(deployAction, solutions);
                String roundDescription = combinedClient.executeUserAction(
                        userInput,
                        deployCallback,
                        deployProcessingRules
                );
                RoundManagement.saveDescription(roundDescription, notifyRecordSystemCallback, writer);
            }
        }  catch (HttpClient.ServerErrorException e) {
            LOG.error("Server experienced an error. Try again.", e);
        } catch (HttpClient.OtherCommunicationException e) {
            LOG.error("Client threw an unexpected error.", e);
        } catch (HttpClient.ClientErrorException e) {
            LOG.error("The client sent something the server didn't expect.");
            writer.println(e.getResponseMessage());
        }
    }

    private ProcessingRules createDeployProcessingRules(ClientAction deployAction, Map<String, UserImplementation> solutions) {
        ProcessingRules deployProcessingRules = new ProcessingRules();

        // Debt - do we need this anymore?
        deployProcessingRules
                .on("display_description")
                .call(p -> RoundManagement.saveDescription(p[0], p[1], writer))
                .then(publish());

        solutions.forEach((methodName, userImplementation) -> deployProcessingRules
                .on(methodName)
                .call(userImplementation)
                .then(deployAction));

        return deployProcessingRules;
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
