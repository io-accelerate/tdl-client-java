package tdl.client.queue.serialization;

import com.google.gson.JsonElement;

/**
 * Created by julianghionoiu on 10/01/2016.
 */
public final class JsonRpcRequest {
    private final String method;
    private final JsonElement[] params;
    private final String id;

    public JsonRpcRequest() {
        this.method = "";
        this.params = new JsonElement[] {};
        this.id = "";
    }

    public String getMethod() {
        return method;
    }

    public JsonElement[] getParams() {
        return params;
    }

    public String getId() {
        return id;
    }
}
