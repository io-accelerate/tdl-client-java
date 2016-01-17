package tdl.client.abstractions.response;

import tdl.client.actions.ClientAction;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class ValidResponse implements Response {
    private String id;
    private Object result;
    private ClientAction clientAction;

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
        return String.format("resp = %s", getResult());
    }

}
