package tdl.client.queue;

import tdl.client.queue.ImplementationRunner;
import tdl.client.runner.IConsoleOut;

public class NoisyImplementationRunner implements ImplementationRunner {
    private final String deployMessage;
    private final IConsoleOut consoleOut;

    public NoisyImplementationRunner(String deployMessage, IConsoleOut consoleOut) {
        this.deployMessage = deployMessage;
        this.consoleOut = consoleOut;
    }
    @Override
    public void run() {
        consoleOut.println(deployMessage);
    }
}
