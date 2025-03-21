package io.accelerate.client.queue.abstractions;

import io.accelerate.client.queue.transport.StringMessage;

import java.util.List;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class Request {
    private final StringMessage originalMessage;
    private final String id;
    private final String methodName;
    private final List<ParamAccessor> params;

    public Request(StringMessage messageText, String id, String method, List<ParamAccessor> params) {
        this.originalMessage = messageText;
        this.id = id;
        this.methodName = method;
        this.params = params;
    }

    public StringMessage getOriginalMessage() {
        return originalMessage;
    }

    public String getId() {
        return id;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<ParamAccessor> getParams() {
        return params;
    }
}
