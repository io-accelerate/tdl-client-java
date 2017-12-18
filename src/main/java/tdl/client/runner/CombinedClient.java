package tdl.client.runner;


class CombinedClient {
    private final IConsoleOut consoleOut;
    private final boolean recordingSystemOn;
    private HttpClient httpClient;
    private IImplementationRunner implementationRunner;

    CombinedClient(String journeyId, boolean useColours, String hostname, int port, IConsoleOut consoleOut, IImplementationRunner implementationRunner, boolean recordingSystemOn) {
        this.consoleOut = consoleOut;
        httpClient = new HttpClient(hostname, port, journeyId, useColours);
        this.implementationRunner = implementationRunner;
        this.recordingSystemOn = recordingSystemOn;
    }

    boolean checkStatusOfChallenge() throws HttpClient.ServerErrorException, HttpClient.OtherCommunicationException, HttpClient.ClientErrorException {
        String journeyProgress = httpClient.getJourneyProgress();
        consoleOut.println(journeyProgress);

        String availableActions = httpClient.getAvailableActions();
        consoleOut.println(availableActions);

        return !availableActions.contains("No actions available.");
    }

    String executeUserAction(String userInput) throws HttpClient.ServerErrorException, HttpClient.OtherCommunicationException, HttpClient.ClientErrorException {
        if (userInput.equals("deploy")) {
            implementationRunner.deployToQueue();
            String lastFetchedRound = RoundManagement.getLastFetchedRound();
            RecordingSystem.deployNotifyEvent(recordingSystemOn, lastFetchedRound);
        }
        return executeAction(userInput);
    }

    private String executeAction(String userInput) throws HttpClient.ServerErrorException, HttpClient.OtherCommunicationException, HttpClient.ClientErrorException {
        String actionFeedback = httpClient.sendAction(userInput);
        consoleOut.println(actionFeedback);
        return httpClient.getRoundDescription();
    }
}
