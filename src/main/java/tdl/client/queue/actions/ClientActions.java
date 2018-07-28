package tdl.client.queue.actions;

/**
 * Created by julianghionoiu on 10/01/2016.
 */
public final class ClientActions {

    private ClientActions() {
        //Utility class constructor
    }

    public static ClientAction publish() {
        return new PublishAction();
    }

    public static ClientAction stop() {
        return new StopAction();
    }

    public static ClientAction publishAndStop() {
        return new PublishAndStopAction();
    }

}
