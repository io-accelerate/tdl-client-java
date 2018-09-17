package tdl.client.runner.connector;

public class EventProcessingException extends Exception {
    public EventProcessingException(String message) {
        super(message);
    }

    EventProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
