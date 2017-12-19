package tdl.client.queue;

import tdl.client.queue.abstractions.UserImplementation;
import tdl.client.runner.IConsoleOut;
import tdl.client.runner.RoundManagement;

import java.util.HashMap;
import java.util.Map;
import static tdl.client.queue.actions.ClientActions.publish;

// TODO merge with QueueClient
public class QueueBasedImplementationRunner implements ImplementationRunner {
    private final Map<String, UserImplementation> solutions;
    private final String username;
    private String hostname;
    private IConsoleOut consoleOut;

    private QueueBasedImplementationRunner(String username) {
        this.username = username;
        this.solutions = new HashMap<>();
    }

    public static QueueBasedImplementationRunner forUsername(String username) {
        return new QueueBasedImplementationRunner(username);
    }

    public QueueBasedImplementationRunner withHostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    public QueueBasedImplementationRunner withSolutionFor(String methodName, UserImplementation solution) {
        solutions.put(methodName, solution);
        return this;
    }

    public QueueBasedImplementationRunner withConsoleOut(IConsoleOut consoleOut) {
        this.consoleOut = consoleOut;
        return this;
    }

    public void run() {
        QueueClient queueClient = new QueueClient.Builder()
                .setHostname(hostname)
                .setUniqueId(username)
                .create();

        ProcessingRules processingRules = createDeployProcessingRules();
        queueClient.goLiveWith(processingRules);
    }

    public ProcessingRules createDeployProcessingRules() {
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
}
