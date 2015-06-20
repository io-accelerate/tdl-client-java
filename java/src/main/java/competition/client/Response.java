package competition.client;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class Response {
    private String requestId;
    private Object result;

    public Response(String requestId, Object result) {
        this.requestId = requestId;
        this.result = result;
    }

    public String getRequestId() {
        return requestId;
    }

    public Object getResult() {
        return result;
    }
}
