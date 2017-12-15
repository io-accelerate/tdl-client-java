package tdl.client.runner;

import tdl.client.ProcessingRules;
import java.io.PrintStream;


public class CombinedClient {
    private HttpClient httpClient;
    private PrintStream writer;
    private ImplementationRunner implementationRunner;

    public CombinedClient(String journeyId, boolean useColours, String hostname, int port, PrintStream writer, ImplementationRunner implementationRunner) {
        this.writer = writer;
        httpClient = new HttpClient(hostname, port, journeyId, useColours);
        this.implementationRunner = implementationRunner;
    }

    public boolean checkStatusOfChallenge() throws HttpClient.ServerErrorException, HttpClient.OtherCommunicationException, HttpClient.ClientErrorException {
        String journeyProgress = httpClient.getJourneyProgress();
        writer.println(journeyProgress);

        String availableActions = httpClient.getAvailableActions();
        writer.println(availableActions);

        return !availableActions.contains("No actions available.");
    }

    public String executeUserAction(String userInput) throws HttpClient.ServerErrorException, HttpClient.OtherCommunicationException, HttpClient.ClientErrorException {
        if (userInput.equals("deploy")) {
            ProcessingRules deployProcessingRules = implementationRunner.createDeployProcessingRules();
            implementationRunner.deployToQueue(deployProcessingRules);
        }
        return executeAction(userInput);
    }

    private String executeAction(String userInput) throws HttpClient.ServerErrorException, HttpClient.OtherCommunicationException, HttpClient.ClientErrorException {
        String actionFeedback = httpClient.sendAction(userInput);
        writer.println(actionFeedback);
        return httpClient.getRoundDescription();
    }
}
