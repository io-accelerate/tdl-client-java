package tdl.client.queue.actions;

import tdl.client.queue.QueueBasedImplementationRunner;
import tdl.client.queue.abstractions.Request;
import tdl.client.queue.abstractions.response.Response;
import tdl.client.queue.transport.BrokerCommunicationException;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Created by julianghionoiu on 10/01/2016.
 */
public class PublishAndStopAction implements ClientAction {
    public String getAuditText() {
        logToConsole("           PublishAndStopAction getAuditText");
        return "(NOT PUBLISHED)";
    }

    private void logToConsole(String s) {
        if (new File("DEBUG").exists()) {
            System.out.println(s);
        }
    }

    @Override
    public void afterResponse(QueueBasedImplementationRunner q, Request request, Response response)
            throws BrokerCommunicationException {
        logToConsole("           PublishAndStopAction afterResponse");
        q.respondTo(request, with(response));
    }

    public List<Request> getNextRequest(QueueBasedImplementationRunner q) {
        logToConsole("           PublishAndStopAction getNextRequest");
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "PublishAndStopAction{}";
    }
}
