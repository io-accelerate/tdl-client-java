package io.accelerate.client.queue.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.accelerate.client.queue.abstractions.ParamAccessor;
import io.accelerate.client.queue.abstractions.Request;
import io.accelerate.client.queue.abstractions.response.Response;
import io.accelerate.client.queue.transport.StringMessage;

import javax.jms.JMSException;
import java.util.List;
import java.util.Optional;

/**
 * Created by julianghionoiu on 24/10/2015.
 */
public class JsonRpcSerializationProvider implements SerializationProvider {
    private final ObjectMapper objectMapper;

    public JsonRpcSerializationProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<Request> deserialize(StringMessage messageText) throws DeserializationException {
        Optional<Request> request = Optional.empty();

        if (messageText.isValid()) {
            try {
                JsonRpcRequest jsonRpcRequest = objectMapper.readValue(messageText.getContent(), JsonRpcRequest.class);
                List<ParamAccessor> paramAccessors = jsonRpcRequest.params().stream().map(jsonNode -> new ParamAccessor(jsonNode, objectMapper)).toList();
                request = Optional.of(new Request(messageText, jsonRpcRequest.id(), jsonRpcRequest.method(), paramAccessors));
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
