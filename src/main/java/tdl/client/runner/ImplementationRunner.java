package tdl.client.runner;

import tdl.client.Client;
import tdl.client.ProcessingRules;
import tdl.client.abstractions.UserImplementation;

import java.util.HashMap;
import java.util.Map;
import static tdl.client.actions.ClientActions.publish;

public class ImplementationRunner {
    private final Map<String, UserImplementation> solutions;
    private final String username;
    private String hostname;
    private IConsoleOut consoleOut;

    private ImplementationRunner(String username) {
        this.username = username;
        this.solutions = new HashMap<>();
    }

    public static ImplementationRunner forUsername(String username) {
        return new ImplementationRunner(username);
    }

    public ImplementationRunner withHostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    public ImplementationRunner withSolutionFor(String methodName, UserImplementation solution) {
        solutions.put(methodName, solution);
        return this;
    }

    public ImplementationRunner withConsoleOut(IConsoleOut consoleOut) {
        this.consoleOut = consoleOut;
        return this;
    }

    ProcessingRules createDeployProcessingRules() {
        ProcessingRules deployProcessingRules = new ProcessingRules();

        // Debt - do we need this anymore?
        deployProcessingRules
                .on("display_description")
                .call(p -> RoundManagement.saveDescription(p[0], p[1], consoleOut))
                .then(publish());

        solutions.forEach((methodName, userImplementation) -> deployProcessingRules
                .on(methodName)
                .call(userImplementation)
                .then(publish()));

        return deployProcessingRules;
    }

    void deployToQueue(ProcessingRules processingRules) {
        Client client = new Client.Builder()
                .setHostname(hostname)
                .setUniqueId(username)
                .create();

        client.goLiveWith(processingRules);
        RecordingSystem.deployNotifyEvent(RoundManagement.getLastFetchedRound(), consoleOut);
    }
}
