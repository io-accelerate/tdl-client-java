package competition.client.respond;

import competition.client.abstractions.Request;
import competition.client.abstractions.Response;

import java.util.Arrays;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class AuditTraffic implements ResponseStrategy {
    private final ResponseStrategy wrappedStrategy;

    public AuditTraffic(ResponseStrategy wrappedStrategy) {
        this.wrappedStrategy = wrappedStrategy;
    }

    @Override
    public Response respondTo(Request request) {
        Response response = wrappedStrategy.respondTo(request);

        System.out.println("id = " + request.getId() + ", " +
                "req = " + Arrays.asList(request.getParams()) + ", " +
                "resp = " + response.getResult());

        return response;
    }
}
