package tdl.client.queue;

import tdl.client.runner.ConsoleOut;

public class NoisyImplementationRunner implements ImplementationRunner {
    private final String deployMessage;
    private final ConsoleOut consoleOut;

    public NoisyImplementationRunner(String deployMessage, ConsoleOut consoleOut) {
        this.deployMessage = deployMessage;
        this.consoleOut = consoleOut;
    }
    @Override
    public void run() {
        consoleOut.println(deployMessage);
    }
}
