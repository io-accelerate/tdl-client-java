package io.accelerate.client.queue.serialization;

import io.accelerate.client.queue.abstractions.Request;
import io.accelerate.client.queue.abstractions.response.Response;
import io.accelerate.client.queue.transport.StringMessage;

import java.util.Optional;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public interface SerializationProvider {

    Optional<Request> deserialize(StringMessage messageText) throws DeserializationException;

    String serialize(Response response) throws SerializationException;
}
