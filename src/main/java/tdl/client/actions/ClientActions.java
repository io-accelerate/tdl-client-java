package tdl.client.actions;

/**
 * Created by julianghionoiu on 10/01/2016.
 */
public class ClientActions {

    public static ClientAction publishAndContinue() {
        return new PublishAction();
    }

    public static ClientAction publish() {
        return publishAndContinue();
    }

    public static ClientAction publishAndStop() {
        return new PublishAndStopAction();
    }

    public static ClientAction stop() {
        return new StopAction();
    }

}
