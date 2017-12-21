package tdl.client.queue.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import tdl.client.queue.abstractions.Request;
import tdl.client.queue.abstractions.response.Response;
import tdl.client.queue.transport.StringMessage;

import javax.jms.JMSException;
import java.util.Optional;

/**
 * Created by julianghionoiu on 24/10/2015.
 */
public class JsonRpcSerializationProvider implements SerializationProvider {
    private final Gson gson;

    public JsonRpcSerializationProvider() {
        gson = new GsonBuilder()
                .serializeNulls()
                .create();
    }

    @Override
    public Optional<Request> deserialize(StringMessage messageText) throws DeserializationException {
        Optional<Request> request = Optional.empty();

        if (messageText.isValid()) {
            try {
                JsonRpcRequest jsonRpcRequest = gson.fromJson(messageText.getContent(), JsonRpcRequest.class);
                request = Optional.of(new Request(messageText, jsonRpcRequest));
            } catch (JsonSyntaxException e) {
                throw new DeserializationException("Invalid message format", e);
            } catch (JMSException e) {
                throw new DeserializationException("Could not obtain message body", e);
            }
        }

        return request;
    }

    @Override
    public String serialize(Response response) {
        return gson.toJson(JsonRpcResponse.from(response));
    }

}
