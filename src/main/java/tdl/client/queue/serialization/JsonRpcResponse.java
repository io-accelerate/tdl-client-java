package tdl.client.queue.serialization;

import tdl.client.queue.abstractions.response.Response;

/**
 * Created by julianghionoiu on 10/01/2016.
 */
@SuppressWarnings("FieldCanBeLocal")
public final class JsonRpcResponse {
    private final Object result;
    private final String id;

    @SuppressWarnings("SameParameterValue")
    private JsonRpcResponse(Object result, String id) {
        this.result = result;
        this.id = id;
    }

    public Response toResponse() {
        return new Response() {
            @Override
            public String getId() {
                return id;
            }

            @Override
            public Object getResult() {
                return result;
            }

            @Override
            public String getAuditText() {
                return null;
            }
        };
    }
}
