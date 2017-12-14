package tdl.client.runner;

import tdl.client.Client;
import tdl.client.ProcessingRules;
import java.io.PrintStream;


public class CombinedClient {
    private HttpClient httpClient;
    private String hostname;
    private String username;
    private PrintStream writer;

    public CombinedClient(String journeyId, boolean useColours, String hostname, int port, String username, PrintStream writer) {
        this.hostname = hostname;
        this.username = username;
        this.writer = writer;
        httpClient = new HttpClient(hostname, port, journeyId, useColours);
    }

    public boolean checkStatusOfChallenge() throws HttpClient.ServerErrorException, HttpClient.OtherCommunicationException, HttpClient.ClientErrorException {
        String journeyProgress = httpClient.getJourneyProgress();
        writer.println(journeyProgress);

        String availableActions = httpClient.getAvailableActions();
        writer.println(availableActions);

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
        writer.println(actionFeedback);
        return httpClient.getRoundDescription();
    }
}
