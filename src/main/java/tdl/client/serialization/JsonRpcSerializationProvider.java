package tdl.client.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import tdl.client.abstractions.Request;
import tdl.client.abstractions.Response;
import tdl.client.transport.StringMessage;

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
    public Optional<Request> deserialize(StringMessage messageText) throws JMSException {
        Optional<Request> request = Optional.empty();

        if (messageText.isValid()) {
            JsonRpcRequest jsonRpcRequest = gson.fromJson(messageText.getContent(), JsonRpcRequest.class);
            request = Optional.of(new Request(messageText, jsonRpcRequest));
        }

        return request;
    }

    @Override
    public String serialize(Response response) {
        String serializedForm = null;
        if ( response != null ){
            serializedForm = gson.toJson(JsonRpcResponse.from(response));
        }
        return serializedForm;
    }

    //~~~~ Gson classes

}
