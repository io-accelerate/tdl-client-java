package tdl.client.queue.actions;

import tdl.client.queue.abstractions.Request;
import tdl.client.queue.abstractions.response.Response;
import tdl.client.queue.transport.BrokerCommunicationException;
import tdl.client.queue.transport.RemoteBroker;

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
        if ((System.getenv("DEBUG") != null) && System.getenv("DEBUG").contains("true")) {
            System.out.println(s);
        }
    }

    @Override
    public void afterResponse(RemoteBroker remoteBroker, Request request, Response response)
            throws BrokerCommunicationException {
        logToConsole("           PublishAndStopAction afterResponse");
        remoteBroker.respondTo(request, with(response));
    }

    public List<Request> getNextRequest(RemoteBroker t) {
        logToConsole("           PublishAndStopAction getNextRequest");
//        return Optional.empty();
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "PublishAndStopAction{}";
    }
}
