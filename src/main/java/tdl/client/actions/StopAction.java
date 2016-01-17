package tdl.client.actions;

import tdl.client.abstractions.Request;
import tdl.client.abstractions.response.Response;
import tdl.client.transport.BrokerCommunicationException;
import tdl.client.transport.RemoteBroker;

import javax.jms.JMSException;
import java.util.Optional;

/**
 * Created by julianghionoiu on 10/01/2016.
 */
public class StopAction implements ClientAction {
    public String getAuditText() {
        return "(NOT PUBLISHED)";
    }

    @Override
    public void afterResponse(RemoteBroker remoteBroker, Request request, Response response)
            throws BrokerCommunicationException {
        //Do nothing
    }

    public Optional<Request> getNextRequest(RemoteBroker t) throws BrokerCommunicationException {
        return Optional.empty();
    }
}
