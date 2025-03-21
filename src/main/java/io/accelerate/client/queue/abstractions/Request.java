package io.accelerate.client.queue.abstractions;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.accelerate.client.audit.Auditable;
import io.accelerate.client.audit.PresentationUtils;
import io.accelerate.client.queue.serialization.JsonRpcRequest;
import io.accelerate.client.queue.transport.StringMessage;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class Request implements Auditable {
    private final StringMessage originalMessage;
    private final JsonRpcRequest requestData;
    private final ObjectMapper objectMapper;
    private final PresentationUtils presentationUtils;

    public Request(StringMessage originalMessage, JsonRpcRequest requestData, ObjectMapper objectMapper) {
        this.originalMessage = originalMessage;
        this.requestData = requestData;
        this.objectMapper = objectMapper;
        this.presentationUtils = new PresentationUtils(objectMapper);
    }

    public StringMessage getOriginalMessage() {
        return originalMessage;
    }

    public String getId() {
        return requestData.id();
    }

    public String getMethodName() {
        return requestData.method();
    }

    public List<ParamAccessor> getParams() {
        return requestData.params().stream().map(jsonNode -> new ParamAccessor(jsonNode, objectMapper)).collect(Collectors.toList());
    }


    //~~~ Pretty print

    @Override
    public String getAuditText() {
        return String.format("id = %s, req = %s(%s)",
                getId(), getMethodName(), presentationUtils.toDisplayableRequest(getParams()));
    }


    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }
}
