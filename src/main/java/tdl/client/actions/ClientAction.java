package tdl.client.actions;

import tdl.client.abstractions.Request;
import tdl.client.abstractions.Response;
import tdl.client.audit.Auditable;
import tdl.client.transport.RemoteBroker;

import javax.jms.JMSException;
import java.util.Optional;

/**
 * Created by julianghionoiu on 10/01/2016.
 */
public interface ClientAction extends Auditable {

    void afterResponse(RemoteBroker remoteBroker, Request request, Response response) throws JMSException;

    Optional<Request> getNextRequest(RemoteBroker t) throws JMSException;


    //~~~ Fluent API

    default <T> T with(T obj) {
        return obj;
    }
}
