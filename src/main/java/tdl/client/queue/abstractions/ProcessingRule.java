package tdl.client.queue.abstractions;

/**
 * Created by julianghionoiu on 20/06/2015.
 */

public class ProcessingRule {
    private final UserImplementation userImplementation;

    public ProcessingRule(UserImplementation userImplementation) {
        this.userImplementation = userImplementation;
    }

    public UserImplementation getUserImplementation() {
        return userImplementation;
    }
}
