package tdl.runner;

/**
 * Created by julianghionoiu on 02/05/2015.
 */
public final class InteractionLogger {
    public static final InteractionLogger INSTANCE = new InteractionLogger();

    private InteractionLogger() {
        //Singleton
    }

    public void received(String value) {
        System.out.println("----: " + value);
        System.out.flush();
    }

    public void sent(String value) {
        System.out.println("++++: " + value);
        System.out.flush();
    }

    public void scriptEcho(String value) {
        System.out.println("@@@@: ECHO - " + value);
        System.out.flush();
    }

    public void scriptResult(ExecutionResult result) {
        result.getStdout().forEach(s -> System.out.println("@@@@: " + s));
        if (result.isFailed()) {
            System.out.println("@@@@: FAILED - Script returned non zero status. STDERR:");
            result.getStderr().forEach(s -> System.out.println("@@@@: " + s));
        }
        System.out.flush();
    }
}

