package tdl.client.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import tdl.client.abstractions.Request;
import tdl.client.abstractions.Response;

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
    public Request deserialize(String messageText) {
        JsonRpcRequest jsonRpcRequest = gson.fromJson(messageText, JsonRpcRequest.class);
        return jsonRpcRequest.asRequest();
    }

    @Override
    public String serialize(Response response) {
        return gson.toJson(JsonRpcResponse.from(response));
    }

    //~~~~ Gson classes

    private static final class JsonRpcRequest {
        private final String method;
        private final String[] params;
        private final String id;

        public JsonRpcRequest(String method, String[] params, String id) {
            this.method = method;
            this.params = params;
            this.id = id;
        }

        public Request asRequest() {
            return new Request(id, method, params);
        }
    }

    private static final class JsonRpcResponse {
        private final Object result;
        private final String error;
        private final String id;

        public JsonRpcResponse(Object result, String error, String id) {
            this.result = result;
            this.error = error;
            this.id = id;
        }

        public static JsonRpcResponse from(Response response) {
            return new JsonRpcResponse(response.getResult(), null, response.getId());
        }
    }
}
