package tdl.client.runner;

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
