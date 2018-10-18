package tdl.client.sqs.queue.serialization;

import java.io.File;
import java.util.Arrays;

/**
 * Created by julianghionoiu on 10/01/2016.
 */
public final class JsonRpcRequest {
    private final String method;
    private final String[] params;
    private final String id;

    public JsonRpcRequest() {
        logToConsole("           JsonRpcRequest creation");
        this.method = "";
        this.params = new String[] {};
        this.id = "";
    }

    private void logToConsole(String s) {
        if (new File("DEBUG").exists()) {
            System.out.println(s);
        }
    }

    public String getMethod() {
        logToConsole("           JsonRpcRequest getMethod "+ method);
        return method;
    }

    public String[] getParams() {
        logToConsole("           JsonRpcRequest getParams " + Arrays.toString(params));
        return params;
    }

    public String getId() {
        logToConsole("           JsonRpcRequest getId " + id);
        return id;
    }
}
