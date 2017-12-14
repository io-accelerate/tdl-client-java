package tdl.runner;

import tdl.client.abstractions.UserImplementation;
import tdl.client.actions.ClientAction;
import tdl.client.runner.ClientRunner;

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

    private ClientRunner clientRunner;
    private String[] commandLineArgs;

    void withServerHostname(@SuppressWarnings("SameParameterValue") String hostname) {
        clientRunner.withServerHostname(hostname);
    }

    void withPort(int port) {
        clientRunner.withPort(port);
    }

    void withJourneyId(String journeyId) {
        clientRunner.withJourneyId(journeyId);
    }

    void withColours(boolean useColours) {
        clientRunner.withColours(useColours);
    }

    void withDeployCallback(Runnable deployCallback) {
        clientRunner.withDeployCallback(deployCallback);
    }

    void withDeployAction(ClientAction deployAction) {
        clientRunner.withDeployAction(deployAction);
    }

    void withRecordingSystemOk(boolean recordingSystemOk) {
        clientRunner.withRecordingSystemOk(recordingSystemOk);
    }

    void withNotifyRecordSystemCallback(Consumer<String> notifyRecordSystemCallback) {
        clientRunner.withNotifyRecordSystemCallback(notifyRecordSystemCallback);
    }

    void withSolutionFor(String methodName, UserImplementation solution) {
        clientRunner.withSolutionFor(methodName, solution);
    }

    void withCommandLineArgs(String[] commandLineArgs) {
        this.commandLineArgs = commandLineArgs;
    }

    private AppThread(String username) {
        super("ClientRunner Thread");
        clientRunner = ClientRunner.forUsername(username);

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
            clientRunner.withOutputStream(out);
            clientRunner.withBufferedReader(reader);
            clientRunner.start(commandLineArgs);

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

