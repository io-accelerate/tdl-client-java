package tdl.client.queue.actions;

import tdl.client.queue.abstractions.Request;
import tdl.client.queue.abstractions.response.Response;
import tdl.client.queue.transport.RemoteBroker;

import java.util.Optional;

/**
 * Created by julianghionoiu on 10/01/2016.
 */
public class StopAction implements ClientAction {
    public String getAuditText() {
        return "(NOT PUBLISHED)";
    }

    @Override
    public void afterResponse(RemoteBroker remoteBroker, Request request, Response response) {
        //Do nothing
    }

    public Optional<Request> getNextRequest(RemoteBroker t) {
        return Optional.empty();
    }
}
