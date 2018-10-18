package tdl.client.sqs.runner;

interface RoundChangesListener {
    void onNewRound(String roundId);
}
