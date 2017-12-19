package tdl.examples;

import tdl.client.queue.QueueBasedImplementationRunner;
import tdl.client.queue.ProcessingRules;
import tdl.client.queue.actions.ClientAction;

import static tdl.client.queue.actions.ClientActions.*;

/**
 * Created by julianghionoiu on 11/06/2015.
 */
public class AddNumbers {

    //~~~~~~~~~~~~~~ Setup ~~~~~~~~~~~~~~

    public static ClientAction publishIf(boolean ready) {
        if (ready) {
            return publish();
        } else {
            return stop();
        }
    }

    public static void main(String[] args) throws Exception {
        boolean ready = false;
        if (args.length > 0) {
            ready = Boolean.getBoolean(args[0]);
        }

        startClient(ready);
    }

    private static void startClient(final boolean ready) {
        QueueBasedImplementationRunner queueBasedImplementationRunner = new QueueBasedImplementationRunner.Builder()
                .setHostname("localhost")
                .setPort(61616)
                .setUniqueId("julian@example.com")
                .create();

        ProcessingRules processingRules = new ProcessingRules() {{
            on("display_description").call(params -> "OK").then(publish());
            on("sums").call(AddNumbers::sum).then(publishIf(ready));
            on("end_round").call(AddNumbers::sum).then(publishAndStop());
        }};

        queueBasedImplementationRunner.goLiveWith(processingRules);
    }

    //~~~~~~~ User implementations ~~~~~~~~~~~~~~

    private static Integer sum(String[] params) {
        Integer x = Integer.parseInt(params[0]);
        Integer y = Integer.parseInt(params[1]);
        return x + y;
    }
}
