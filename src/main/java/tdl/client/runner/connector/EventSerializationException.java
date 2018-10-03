package tdl.client.runner.connector;

public class EventSerializationException extends Exception {
    public EventSerializationException(String message, Exception e) {
        super(message, e);
    }

    public EventSerializationException(String message) {
        super(message);
    }
}
