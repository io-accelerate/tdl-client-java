package tdl.runner;

import tdl.client.runner.ImplementationRunner;

import java.io.IOException;
import java.util.function.Consumer;
import static org.hamcrest.CoreMatchers.containsString;


/**
 * This class abstracts the app
 * Created by julianghionoiu on 28/02/2015.
 *
 */
public class ConsoleDriver {
    private AppThread appThread;
    private Console console;

    private ConsoleDriver(String username) {
        appThread = AppThread.using(username);
    }

    static ConsoleDriver forUsername(String username) {
        return new ConsoleDriver(username);
    }

    ConsoleDriver withServerHostname(String hostname) {
        appThread.withServerHostname(hostname);
        return this;
    }

    ConsoleDriver withPort(int port) {
        appThread.withPort(port);
        return this;
    }

    ConsoleDriver withJourneyId(String journeyId) {
        appThread.withJourneyId(journeyId);
        return this;
    }

    ConsoleDriver withColours(boolean useColours) {
        appThread.withColours(useColours);
        return this;
    }

    ConsoleDriver withCommandLineArgs(String[] commandLineArgs) {
        appThread.withCommandLineArgs(commandLineArgs);
        return this;
    }

    ConsoleDriver withImplementationRunner(ImplementationRunner implementationRunner) {
        appThread.withImplementationRunner(implementationRunner);
        return this;
    }

    void startApp(int lifetime) throws InteractionException {
        try {
            appThread.start();
            appThread.startAutoDestroyTimer(lifetime,
                    () -> System.err.println("Warning ! The AppThread self destruct timer was triggered."));

            console = Console.from(appThread);
        } catch (Exception e) {
            throw new InteractionException("Failed to start app", e);
        }
    }

    void writeLine(String line) {
        try {
            console.writeLine(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void readLinesUntilLine(String s) throws IOException, InteractionException {
        console.readLinesUntilLine(containsString(s));
    }

    void waitForAppToStop() throws InteractionException {
        if (appThread == null) {
            return;
        }

        try {
            appThread.join();
        } catch (Exception e) {
            throw new InteractionException("Failed to stop app", e);
        }
    }

    void killApp() throws InteractionException {
        if (appThread == null) {
            return;
        }

        try {
            appThread.destroyForcibly(() -> System.out.println("The appThread has been destroyed"));
        } catch (Exception e) {
            throw new InteractionException("Failed to kill app", e);
        }

    }
}

