package competition.client.respond;

import competition.client.abstractions.Request;
import competition.client.abstractions.Response;

import java.util.Arrays;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class AuditTraffic implements RespondToRequest {
    private final RespondToRequest wrappedObject;

    public AuditTraffic(RespondToRequest wrappedObject) {
        this.wrappedObject = wrappedObject;
    }

    @Override
    public Response respondTo(Request request) {
        Response response = wrappedObject.respondTo(request);

        System.out.println("id = " + request.getRequestId() + ", " +
                "req = " + Arrays.asList(request.getParams()) + ", " +
                "resp = " + response.getResult());

        return response;
    }
}
