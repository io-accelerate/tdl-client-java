package tdl.client.runner;

import tdl.client.Client;
import tdl.client.ProcessingRules;
import tdl.client.abstractions.UserImplementation;
import tdl.client.actions.ClientAction;

import java.util.Map;
import java.util.function.Consumer;

import static tdl.client.actions.ClientActions.publish;

public class CombinedClient {
    private HttpClient httpClient;
    private String hostname;
    private String username;
    private Consumer<String> printer;

    public CombinedClient(String journeyId, boolean useColours, String hostname, int port, String username, Consumer<String> printer) {
        this.hostname = hostname;
        this.username = username;
        this.printer = printer;
        httpClient = new HttpClient(hostname, port, journeyId, useColours);
    }

    public boolean checkStatusOfChallenge() throws HttpClient.ServerErrorException, HttpClient.OtherCommunicationException, HttpClient.ClientErrorException {
        String journeyProgress = httpClient.getJourneyProgress();
        printer.accept(journeyProgress);

        String availableActions = httpClient.getAvailableActions();
        printer.accept(availableActions);

        return !availableActions.contains("No actions available.");
    }

    public String executeUserAction(String userInput, Runnable deployCallback, ProcessingRules processingRules) throws HttpClient.ServerErrorException, HttpClient.OtherCommunicationException, HttpClient.ClientErrorException {
        if (userInput.equals("deploy")) {
            deployToQueue(deployCallback, processingRules);
        }
        return executeAction(userInput);
    }

    private void deployToQueue(Runnable deployCallback, ProcessingRules processingRules) {
        Client client = new Client.Builder()
                .setHostname(hostname)
                .setUniqueId(username)
                .create();

        client.goLiveWith(processingRules);
        deployCallback.run();
    }

    private String executeAction(String userInput) throws HttpClient.ServerErrorException, HttpClient.OtherCommunicationException, HttpClient.ClientErrorException {
        String actionFeedback = httpClient.sendAction(userInput);
        printer.accept(actionFeedback);
        return httpClient.getRoundDescription();
    }

    public ProcessingRules createDeployProcessingRules(UserImplementation saveDescriptionUserImplementation, ClientAction deployAction, Map<String, UserImplementation> solutions) {
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

