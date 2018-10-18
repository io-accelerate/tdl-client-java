package tdl.client.jms.queue.serialization;

import tdl.client.jms.queue.abstractions.response.Response;

/**
 * Created by julianghionoiu on 10/01/2016.
 */
@SuppressWarnings("FieldCanBeLocal")
final class JsonRpcResponse {
    private final Object result;
    private final String error;
    private final String id;

    @SuppressWarnings("SameParameterValue")
    private JsonRpcResponse(Object result, String error, String id) {
        this.result = result;
        this.error = error;
        this.id = id;
    }

    static JsonRpcResponse from(Response response) {
        return new JsonRpcResponse(response.getResult(), null, response.getId());
    }
}
