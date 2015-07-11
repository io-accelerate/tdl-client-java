package competition.client.abstractions;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class Request {
    private final String requestId;
    private final String[] params;

    public Request(String requestId, String[] params) {
        this.requestId = requestId;
        this.params = params;
    }

    public String getRequestId() {
        return requestId;
    }

    public String[] getParams() {
        return params;
    }
}
