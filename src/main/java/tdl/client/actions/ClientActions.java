package tdl.client.actions;

/**
 * Created by julianghionoiu on 10/01/2016.
 */
public final class ClientActions {
    public static ClientAction PUBLISH = new PublishAction();
    public static ClientAction STOP = new StopAction();
    public static ClientAction PUBLISH_AND_STOP = new PublishAndStopAction();

    private ClientActions() {
        //Utility class constructor
    }

    public static ClientAction publish() {
        return PUBLISH;
    }

    public static ClientAction stop() {
        return STOP;
    }

    public static ClientAction publishAndStop() {
        return PUBLISH_AND_STOP;
    }

}
