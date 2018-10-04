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
public class PublishAction implements ClientAction {
    public String getAuditText() {
        return "";
    }

    @Override
    public void afterResponse(QueueBasedImplementationRunner queueBasedImplementationRunner, Request request, Response response)
            throws BrokerCommunicationException {
        logToConsole("           PublishAction afterResponse");
        queueBasedImplementationRunner.respondTo(request, with(response));
    }

    private void logToConsole(String s) {
        if (new File("DEBUG").exists()) {
            System.out.println(s);
        }
    }

    public List<Request> getNextRequest(QueueBasedImplementationRunner queueBasedImplementationRunner) throws BrokerCommunicationException {
        logToConsole("           PublishAction getNextRequest");
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "PublishAction{}";
    }
}
