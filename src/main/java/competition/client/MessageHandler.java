package competition.client;

/**
 * Created by julianghionoiu on 22/07/2015.
 */
@FunctionalInterface
public interface MessageHandler {
    String respondTo(String messageText);
}
