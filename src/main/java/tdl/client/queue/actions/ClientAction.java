package tdl.client.queue.actions;

import tdl.client.queue.abstractions.Request;
import tdl.client.queue.abstractions.response.Response;
import tdl.client.audit.Auditable;
import tdl.client.queue.transport.BrokerCommunicationException;
import tdl.client.queue.transport.RemoteBroker;

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
