package tdl.client.abstractions;

import tdl.client.audit.Auditable;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class Response implements Auditable {
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

    //~~~ Pretty print

    @Override
    public String getAuditText() {
        return String.format("resp = %s", getResult());
    }

}
