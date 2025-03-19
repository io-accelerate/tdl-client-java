package io.accelerate.client.queue.serialization;

import io.accelerate.client.queue.abstractions.response.Response;

/**
 * Created by julianghionoiu on 10/01/2016.
 */
@SuppressWarnings("FieldCanBeLocal")
public record JsonRpcResponse(Object result, String error, String id) {
    static JsonRpcResponse from(Response response) {
        return new JsonRpcResponse(response.getResult(), null, response.getId());
    }
}
