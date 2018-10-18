package tdl.client.sqs.runner.connector;

public class EventSendingFailureException extends Exception {
    public EventSendingFailureException(String message, Exception e) {
        super(message, e);
    }
}
