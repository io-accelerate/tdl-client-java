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
        logToConsole("     ValidResponse creation");
        this.id = id;
        this.result = result;
        this.clientAction = clientAction;
    }

    public void logToConsole(String s) {
        if ((System.getenv("DEBUG") != null) && System.getenv("DEBUG").contains("true")) {
            System.out.println(s);
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Object getResult() {
        logToConsole("     ValidResponse getResult");
        return result;
    }

    //~~~ Pretty print


    @Override
    public ClientAction getClientAction() {
        logToConsole("     ValidResponse getClientAction");
        return clientAction;
    }

    @Override
    public String getAuditText() {
        logToConsole("     ValidResponse getAuditText");
        return String.format("resp = %s", PresentationUtils.toDisplayableString(result));
    }

}
