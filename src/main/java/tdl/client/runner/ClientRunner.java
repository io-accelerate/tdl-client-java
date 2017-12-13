package tdl.client.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tdl.client.ProcessingRules;
import tdl.client.abstractions.UserImplementation;
import tdl.client.actions.ClientAction;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static tdl.client.actions.ClientActions.publish;


public class ClientRunner {
    private final Logger LOG = LoggerFactory.getLogger(ClientRunner.class);
    private String hostname;
    private int port;
    private String journeyId;
    private boolean useColours;
    private Runnable deployCallback;
    private Function<String[], String> getUserInput;
    private boolean recordingSystemOk;
    private final String username;
    private final Map<String, UserImplementation> solutions;
    private ClientAction deployAction;
    private UserImplementation saveDescriptionImplementation;
    private Consumer<String> saveDescriptionRoundManagement;
    private Consumer<String> printer;

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

    public ClientRunner withUserInput(Function<String[], String> getUserInput) {
        this.getUserInput = getUserInput;
        return this;
    }

    public ClientRunner withPrinter(Consumer<String> printer) {
        this.printer = printer;
        return this;
    }

    public ClientRunner withDeployAction(ClientAction deployAction) {
        this.deployAction = deployAction;
        return this;
    }

    public ClientRunner withRecordingSystemOk(boolean recordingSystemOk) {
        this.recordingSystemOk = recordingSystemOk;
        return this;
    }

    public ClientRunner withSaveDescriptionImplementation(UserImplementation saveDescriptionUserImplementation) {
        this.saveDescriptionImplementation = saveDescriptionUserImplementation;
        return this;
    }

    public ClientRunner withSaveDescriptionRoundManagement(Consumer<String> saveDescriptionRoundManagement) {
        this.saveDescriptionRoundManagement = saveDescriptionRoundManagement;
        return this;
    }

    public ClientRunner withSolutionFor(String methodName, UserImplementation solution) {
        solutions.put(methodName, solution);
        return this;
    }

    //~~~~~~~~ The entry point ~~~~~~~~~

    public void start(String[] args) {
        if (!this.recordingSystemOk) {
            printer.accept("Please run `record_screen_and_upload` before continuing.");
            return;
        }
        printer.accept("Connecting to " + hostname);
        runApp(args);
    }

    private void runApp(String[] args) {
        CombinedClient combinedClient = new CombinedClient(journeyId, useColours, hostname, port, username, printer);

        try {
            boolean shouldContinue = combinedClient.checkStatusOfChallenge();
            if (shouldContinue) {
                String userInput = getUserInput.apply(args);
                ProcessingRules deployProcessingRules = createDeployProcessingRules(saveDescriptionImplementation, deployAction, solutions);
                String roundDescription = combinedClient.executeUserAction(
                        userInput,
                        deployCallback,
                        deployProcessingRules
                );
                saveDescriptionRoundManagement.accept(roundDescription);
            }
        } catch (HttpClient.ServerErrorException e) {
            LOG.error("Server experienced an error. Try again.", e);
        } catch (HttpClient.OtherCommunicationException e) {
            LOG.error("Client threw an unexpected error.", e);
        } catch (HttpClient.ClientErrorException e) {
            LOG.error("The client sent something the server didn't expect.");
            printer.accept(e.getResponseMessage());
        }
    }

    private ProcessingRules createDeployProcessingRules(UserImplementation saveDescriptionUserImplementation, ClientAction deployAction, Map<String, UserImplementation> solutions) {
        ProcessingRules deployProcessingRules = new ProcessingRules();

        // Debt - do we need this anymore?
        deployProcessingRules
                .on("display_description")
                .call(saveDescriptionUserImplementation)
                .then(publish());

        solutions.forEach((methodName, userImplementation) -> deployProcessingRules
                .on(methodName)
                .call(userImplementation)
                .then(deployAction));

        return deployProcessingRules;
    }
}