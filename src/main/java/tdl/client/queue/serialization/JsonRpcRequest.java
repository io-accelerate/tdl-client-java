package tdl.client.queue.serialization;

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by julianghionoiu on 10/01/2016.
 */
public final class JsonRpcRequest {
    private final String method;
    private final List<JsonElement> params;
    private final String id;

    public JsonRpcRequest() {
        this.method = "";
        this.params = new ArrayList<>();
        this.id = "";
    }

    public String getMethod() {
        return method;
    }

    public List<JsonElement> getParams() {
        return params;
    }

    public String getId() {
        return id;
    }
}
