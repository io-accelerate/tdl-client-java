package tdl.client.abstractions;

import tdl.client.actions.ClientAction;

/**
 * Created by julianghionoiu on 20/06/2015.
 */

public class ProcessingRule {
    private final UserImplementation userImplementation;
    private final ClientAction clientAction;

    public ProcessingRule(UserImplementation userImplementation, ClientAction clientAction) {
        this.userImplementation = userImplementation;
        this.clientAction = clientAction;
    }

    public UserImplementation getUserImplementation() {
        return userImplementation;
    }

    public ClientAction getClientAction() {
        return clientAction;
    }
}
