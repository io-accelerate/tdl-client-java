package competition.client.respond;

import competition.client.abstractions.Request;
import competition.client.abstractions.Response;

import java.io.PrintStream;
import java.util.Arrays;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class AuditTraffic implements ResponseStrategy {
    private final ResponseStrategy wrappedStrategy;
    private final PrintStream auditStream;

    public AuditTraffic(PrintStream auditStream, ResponseStrategy wrappedStrategy) {
        this.auditStream = auditStream;
        this.wrappedStrategy = wrappedStrategy;
    }

    @Override
    public Response respondTo(Request request) {
        Response response = wrappedStrategy.respondTo(request);

        auditStream.println("id = " + request.getId() + ", " +
                "req = " + Arrays.asList(request.getParams()) + ", " +
                "resp = " + response.getResult());

        return response;
    }
}
