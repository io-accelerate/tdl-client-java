package acceptance.runner;

public class InteractionException extends Exception {

    public InteractionException(String message) {
        super(message);
    }

    public InteractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
