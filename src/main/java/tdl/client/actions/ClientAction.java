package tdl.client.actions;

import tdl.client.abstractions.Request;
import tdl.client.abstractions.response.Response;
import tdl.client.audit.Auditable;
import tdl.client.serialization.DeserializationException;
import tdl.client.transport.BrokerCommunicationException;
import tdl.client.transport.RemoteBroker;

import javax.jms.JMSException;
import java.util.Optional;

/**
 * Created by julianghionoiu on 10/01/2016.
 */
public interface ClientAction extends Auditable {

    void afterResponse(RemoteBroker remoteBroker, Request request, Response response) throws BrokerCommunicationException;

    Optional<Request> getNextRequest(RemoteBroker t) throws BrokerCommunicationException;


    //~~~ Fluent API

    default <T> T with(T obj) {
        return obj;
    }
}
