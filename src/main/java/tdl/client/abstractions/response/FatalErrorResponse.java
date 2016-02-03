package tdl.client.abstractions.response;

import tdl.client.actions.ClientAction;
import tdl.client.actions.ClientActions;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class FatalErrorResponse implements Response {
    private final String message;

    public FatalErrorResponse(String message) {
        this.message = message;
    }

    @Override
    public String getId() {
        return "error";
    }

    @Override
    public Object getResult() {
        return message;
    }

    //~~~ Pretty print

    @Override
    public ClientAction getClientAction() {
        return ClientActions.stop();
    }

    @Override
    public String getAuditText() {
        return String.format("%s = \"%s\"",  getId(), getResult() );
    }

}
