package tdl.client.runner;

import tdl.client.actions.ClientAction;
import tdl.client.actions.ClientActions;

public enum RunnerAction {
    getNewRoundDescription("new", ClientActions.stop()),
    deployToProduction("deploy", ClientActions.publish());

    private String shortName;
    private ClientAction clientAction;

    RunnerAction(String shortName, ClientAction clientAction) {
        this.shortName = shortName;
        this.clientAction = clientAction;
    }

    public String getShortName() {
        return shortName;
    }

    public ClientAction getClientAction() {
        return clientAction;
    }
}
