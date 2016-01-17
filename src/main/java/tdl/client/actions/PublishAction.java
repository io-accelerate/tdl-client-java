package tdl.client.actions;

import tdl.client.abstractions.Request;
import tdl.client.abstractions.response.Response;
import tdl.client.serialization.DeserializationException;
import tdl.client.transport.BrokerCommunicationException;
import tdl.client.transport.RemoteBroker;

import javax.jms.JMSException;
import java.util.Optional;

/**
 * Created by julianghionoiu on 10/01/2016.
 */
public class PublishAction implements ClientAction {
    public String getAuditText() {
        return "";
    }

    @Override
    public void afterResponse(RemoteBroker remoteBroker, Request request, Response response)
            throws BrokerCommunicationException {
        remoteBroker.respondTo(request, with(response));
    }

    public Optional<Request> getNextRequest(RemoteBroker broker) throws BrokerCommunicationException {
        return broker.receive();
    }
}
