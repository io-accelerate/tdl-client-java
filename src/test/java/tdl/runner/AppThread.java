package tdl.runner;

import tdl.client.runner.ChallengeSession;
import tdl.client.runner.ImplementationRunner;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;

public class AppThread extends Thread {
    //To app
    private final PipedInputStream threadToApp;
    private final PipedOutputStream clientToThread;

    //From app
    private final PipedInputStream threadToClient;
    private final PipedOutputStream appToThread;

    private ChallengeSession challengeSession;
    private String[] commandLineArgs;

    void withServerHostname(@SuppressWarnings("SameParameterValue") String hostname) {
        challengeSession.withServerHostname(hostname);
    }

    void withPort(int port) {
        challengeSession.withPort(port);
    }

    void withJourneyId(String journeyId) {
        challengeSession.withJourneyId(journeyId);
    }

    void withColours(boolean useColours) {
        challengeSession.withColours(useColours);
    }

    void withImplementationRunner(ImplementationRunner implementationRunner) {
        challengeSession.withImplementationRunner(implementationRunner);
    }

    void withCommandLineArgs(String[] commandLineArgs) {
        this.commandLineArgs = commandLineArgs;
    }

    private AppThread(String username) {
        super("ChallengeSession Thread");
        challengeSession = ChallengeSession.forUsername(username);

        try {
            // client writes -> thread <- app reads
            this.threadToApp = new PipedInputStream();
            this.clientToThread = new PipedOutputStream(threadToApp);

            // client reads -> thread <- app writes
            this.threadToClient = new PipedInputStream();
            this.appToThread = new PipedOutputStream(threadToClient);
        } catch ( IOException e ){
            throw new IllegalStateException("Failed to create streams");
        }
    }

    static AppThread using(String username) {
        return new AppThread(username);
    }

    public void run() {
        try {
            PrintStream out = new PrintStream(appToThread);
            BufferedReader reader = new BufferedReader(new InputStreamReader(threadToApp));
            System.out.println("Starting app with: " + Arrays.toString(commandLineArgs));
            // start in a background thread, check if thread finished for exit.
            challengeSession.withOutputStream(out);
            challengeSession.withBufferedReader(reader);
            challengeSession.start(commandLineArgs);

        } catch (Exception e) {
            System.err.println("Top level exception in CommandLineApp: ");
            e.printStackTrace();
        }
    }

    InputStream getInputStream() {
        return threadToClient;
    }

    OutputStream getOutputStream() {
        return clientToThread;
    }

    void destroyForcibly(EventNotifier destructionNotifier) {
        //Close all streams and interrupt thread
        try {
            threadToApp.close();
            appToThread.close();
            clientToThread.close();
            threadToClient.close();
        } catch (IOException e) {
            System.err.println("Exception when closing stream for: "+AppThread.this.getName());
            e.printStackTrace();
        }

        if (AppThread.this.isAlive()) {
            AppThread.this.interrupt();
            destructionNotifier.eventHappened();
        }
    }

    void startAutoDestroyTimer(Integer gracePeriod, EventNotifier destructionNotifier) {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                AppThread.this.destroyForcibly(destructionNotifier);
                timer.cancel();
            }
        };
        timer.schedule(task, gracePeriod);
    }
}

