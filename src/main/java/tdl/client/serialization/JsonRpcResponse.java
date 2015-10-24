package tdl.client.serialization;

import tdl.client.abstractions.Response;

/**
 * Created by julianghionoiu on 10/01/2016.
 */
public final class JsonRpcResponse {
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
