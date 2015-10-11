package tdl.client.abstractions;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class Request {
    private final String id;
    private final String[] params;

    public Request(String id, String[] params) {
        this.id = id;
        this.params = params;


        System.console();
    }

    public String getId() {
        return id;
    }

    public String[] getParams() {
        return params;
    }
}
