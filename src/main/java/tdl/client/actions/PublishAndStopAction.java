package tdl.client.actions;

import tdl.client.abstractions.Request;
import tdl.client.abstractions.Response;
import tdl.client.transport.RemoteBroker;

import javax.jms.JMSException;
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
            throws JMSException {
        remoteBroker.respondTo(request, with(response));
    }

    public Optional<Request> getNextRequest(RemoteBroker t) throws JMSException {
        return Optional.empty();
    }
}
