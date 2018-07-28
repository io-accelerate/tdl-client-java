package tdl.client.queue.actions;

import tdl.client.queue.abstractions.Request;
import tdl.client.queue.abstractions.response.Response;
import tdl.client.queue.transport.BrokerCommunicationException;
import tdl.client.queue.transport.RemoteBroker;

import java.util.Optional;

/**
 * Created by julianghionoiu on 10/01/2016.
 */
public class PublishAndStopAction implements ClientAction {
    public String getAuditText() {
        return "(NOT PUBLISHED)";
    }

    @Override
    public void afterResponse(RemoteBroker remoteBroker, Request request, Response response)
            throws BrokerCommunicationException {
        remoteBroker.respondTo(request, with(response));
    }

    public Optional<Request> getNextRequest(RemoteBroker t) {
        return Optional.empty();
    }
}
