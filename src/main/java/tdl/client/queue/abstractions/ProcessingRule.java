package tdl.client.queue.abstractions;

import tdl.client.queue.actions.ClientAction;

import java.io.File;

/**
 * Created by julianghionoiu on 20/06/2015.
 */

public class ProcessingRule {
    private final UserImplementation userImplementation;
    private final ClientAction clientAction;

    public ProcessingRule(UserImplementation userImplementation, ClientAction clientAction) {
        logToConsole("           ProcessingRule creation");
        this.userImplementation = userImplementation;
        this.clientAction = clientAction;
    }

    public void logToConsole(String s) {
        if (new File("DEBUG").exists()) {
            System.out.println(s);
        }
    }

    public UserImplementation getUserImplementation() {
        logToConsole("           ProcessingRule getUserImplementation");
        return userImplementation;
    }

    public ClientAction getClientAction() {
        logToConsole("           ProcessingRule getClientAction");
        return clientAction;
    }

    @Override
    public String toString() {
        return "ProcessingRule{" +
                "userImplementation=" + userImplementation +
                ", clientAction=" + clientAction +
                '}';
    }
}
