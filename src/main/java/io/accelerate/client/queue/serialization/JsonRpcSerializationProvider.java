package io.accelerate.client.queue.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import io.accelerate.client.queue.abstractions.Request;
import io.accelerate.client.queue.abstractions.response.Response;
import io.accelerate.client.queue.transport.StringMessage;

import javax.jms.JMSException;
import java.util.Optional;

/**
 * Created by julianghionoiu on 24/10/2015.
 */
public class JsonRpcSerializationProvider implements SerializationProvider {
    private final ObjectMapper objectMapper;

    public JsonRpcSerializationProvider() {
        objectMapper = new ObjectMapper();
    }

    @Override
    public Optional<Request> deserialize(StringMessage messageText) throws DeserializationException {
        Optional<Request> request = Optional.empty();

        if (messageText.isValid()) {
            try {
                JsonRpcRequest jsonRpcRequest = objectMapper.readValue(messageText.getContent(), JsonRpcRequest.class);
                request = Optional.of(new Request(messageText, jsonRpcRequest));
            } catch (JMSException | JsonProcessingException e) {
                throw new DeserializationException("Invalid message format", e);
            }
        }

        return request;
    }

    @Override
    public String serialize(Response response) throws SerializationException {
        try {
            return objectMapper.writeValueAsString(JsonRpcResponse.from(response));
        } catch (JsonProcessingException e) {
            throw new SerializationException("Could not serialise respone", e);
        }
    }

}
