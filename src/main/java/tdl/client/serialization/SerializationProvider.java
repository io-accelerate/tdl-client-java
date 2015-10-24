package tdl.client.serialization;

import tdl.client.abstractions.Request;
import tdl.client.abstractions.Response;
import tdl.client.transport.StringMessage;

import javax.jms.JMSException;
import java.util.Optional;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public interface SerializationProvider {

    Optional<Request> deserialize(StringMessage messageText) throws JMSException;

    String serialize(Response response);
}
