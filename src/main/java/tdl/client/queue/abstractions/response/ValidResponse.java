package tdl.client.queue.abstractions.response;

import tdl.client.audit.PresentationUtils;

import java.io.File;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class ValidResponse implements Response {
    private final String id;
    private final Object result;

    public ValidResponse(String id, Object result) {
        logToConsole("     ValidResponse creation");
        this.id = id;
        this.result = result;
    }

    public void logToConsole(String s) {
        if (new File("DEBUG").exists()) {
            System.out.println(s);
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Object getResult() {
        logToConsole("     ValidResponse getResult");
        return result;
    }

    //~~~ Pretty print

    @Override
    public String getAuditText() {
        logToConsole("     ValidResponse getAuditText");
        return String.format("resp = %s", PresentationUtils.toDisplayableString(result));
    }
}
