package tdl.client.abstractions;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class Request {
    private final String id;
    private final String[] params;
    private String methodName;

    public Request(String id, String methodName, String[] params) {
        this.id = id;
        this.methodName = methodName;
        this.params = params;
    }

    public String getId() {
        return id;
    }

    public String getMethodName() {
        return methodName;
    }

    public String[] getParams() {
        return params;
    }
}
