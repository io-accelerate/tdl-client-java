package tdl.client.queue.abstractions;

import java.io.File;

/**
 * Created by julianghionoiu on 20/06/2015.
 */

public class ProcessingRule {
    private final UserImplementation userImplementation;

    public ProcessingRule(UserImplementation userImplementation) {
        logToConsole("           ProcessingRule creation");
        this.userImplementation = userImplementation;
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

    @Override
    public String toString() {
        return "ProcessingRule{" +
                "userImplementation=" + userImplementation +
                '}';
    }
}
