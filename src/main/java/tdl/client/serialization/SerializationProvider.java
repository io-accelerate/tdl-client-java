package tdl.client.serialization;

import tdl.client.abstractions.Request;
import tdl.client.abstractions.response.Response;
import tdl.client.transport.StringMessage;

import java.util.Optional;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public interface SerializationProvider {

    Optional<Request> deserialize(StringMessage messageText) throws DeserializationException;

    String serialize(Response response);
}
