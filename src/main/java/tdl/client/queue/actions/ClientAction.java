package tdl.client.queue.actions;

import tdl.client.audit.Auditable;
import tdl.client.queue.QueueBasedImplementationRunner;
import tdl.client.queue.abstractions.Request;
import tdl.client.queue.abstractions.response.Response;
import tdl.client.queue.transport.BrokerCommunicationException;

import java.util.List;

/**
 * Created by julianghionoiu on 10/01/2016.
 */
public interface ClientAction extends Auditable {

    void afterResponse(QueueBasedImplementationRunner queueBasedImplementationRunner, Request request, Response response) throws BrokerCommunicationException;

    List<Request> getNextRequest(QueueBasedImplementationRunner q) throws BrokerCommunicationException;


    //~~~ Fluent API

    default <T> T with(T obj) {
        return obj;
    }
}
