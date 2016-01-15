package tdl.client.serialization;

/**
 * Created by julianghionoiu on 10/01/2016.
 */
public final class JsonRpcRequest {
    private final String method;
    private final String[] params;
    private final String id;

    public JsonRpcRequest() {
        this.method = "";
        this.params = new String[] {};
        this.id = "";
    }

    public String getMethod() {
        return method;
    }

    public String[] getParams() {
        return params;
    }

    public String getId() {
        return id;
    }
}
