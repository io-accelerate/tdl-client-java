package tdl.client.queue.abstractions.response;

import tdl.client.queue.actions.ClientAction;
import tdl.client.audit.PresentationUtils;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class ValidResponse implements Response {
    private final String id;
    private final Object result;
    private final ClientAction clientAction;

    public ValidResponse(String id, Object result, ClientAction clientAction) {
        this.id = id;
        this.result = result;
        this.clientAction = clientAction;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Object getResult() {
        return result;
    }

    //~~~ Pretty print


    @Override
    public ClientAction getClientAction() {
        return clientAction;
    }

    @Override
    public String getAuditText() {
        return String.format("resp = %s", PresentationUtils.toDisplayableString(result));
    }

}
