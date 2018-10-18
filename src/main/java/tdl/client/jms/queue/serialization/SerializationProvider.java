package tdl.client.jms.queue.serialization;

import tdl.client.jms.queue.abstractions.Request;
import tdl.client.jms.queue.abstractions.response.Response;
import tdl.client.jms.queue.transport.StringMessage;

import java.util.Optional;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public interface SerializationProvider {

    Optional<Request> deserialize(StringMessage messageText) throws DeserializationException;

    String serialize(Response response);
}
