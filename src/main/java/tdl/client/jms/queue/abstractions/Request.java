package tdl.client.jms.queue.abstractions;

import audit.Auditable;
import audit.PresentationUtils;
import tdl.client.jms.queue.serialization.JsonRpcRequest;
import tdl.client.jms.queue.transport.StringMessage;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class Request implements Auditable {
    private final StringMessage originalMessage;
    private final JsonRpcRequest requestData;

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


    //~~~ Pretty print

    @Override
    public String getAuditText() {
        return String.format("id = %s, req = %s(%s)",
                getId(), getMethodName(), PresentationUtils.toDisplayableString(getParams()));
    }


}
