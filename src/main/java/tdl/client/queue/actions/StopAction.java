package tdl.client.queue.actions;

import tdl.client.queue.QueueBasedImplementationRunner;
import tdl.client.queue.abstractions.Request;
import tdl.client.queue.abstractions.response.Response;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Created by julianghionoiu on 10/01/2016.
 */
public class StopAction implements ClientAction {
    public String getAuditText() {
        logToConsole("           StopAction getAuditText");
        return "(NOT PUBLISHED)";
    }

    private void logToConsole(String s) {
        if (new File("DEBUG").exists()) {
            System.out.println(s);
        }
    }

    @Override
    public void afterResponse(QueueBasedImplementationRunner q, Request request, Response response) {
        logToConsole("           StopAction afterResponse");
        //Do nothing
    }

    public List<Request> getNextRequest(QueueBasedImplementationRunner q) {
        logToConsole("           StopAction getNextRequest");
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "StopAction{}";
    }
}
