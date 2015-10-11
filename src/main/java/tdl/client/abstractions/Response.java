package tdl.client.abstractions;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class Response {
    private String id;
    private Object result;

    public Response(String id, Object result) {
        this.id = id;
        this.result = result;
    }

    public String getId() {
        return id;
    }

    public Object getResult() {
        return result;
    }
}
