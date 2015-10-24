package tdl.client.abstractions;

import tdl.client.serialization.JsonRpcRequest;
import tdl.client.transport.StringMessage;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class Request {
    private StringMessage originalMessage;
    private JsonRpcRequest requestData;

    public Request(StringMessage originalMessage, JsonRpcRequest requestData) {
        this.originalMessage = originalMessage;
        this.requestData = requestData;
    }

    public StringMessage getOriginalMessage() {
        return originalMessage;
    }

    public String getId() {
        return requestData.getId();
    }

    public String getMethodName() {
        return requestData.getMethod();
    }

    public String[] getParams() {
        return requestData.getParams();
    }
}
