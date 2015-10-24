package tdl.client.respond;

import tdl.client.abstractions.Request;
import tdl.client.abstractions.Response;
import tdl.client.audit.AuditStream;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class AuditTraffic implements ResponseStrategy {
    private final ResponseStrategy wrappedStrategy;
    private final AuditStream auditStream;

    public AuditTraffic(AuditStream auditStream, ResponseStrategy wrappedStrategy) {
        this.auditStream = auditStream;
        this.wrappedStrategy = wrappedStrategy;
    }

    @Override
    public Response respondTo(Request request) {
        Response response = wrappedStrategy.respondTo(request);

        auditStream.printf("id = %s, req = %s(%s), resp = %s%n",
                request.getId(), request.getMethodName(), paramsToString(request), response.getResult());

        return response;
    }

    //~~~ Utils

    private static String paramsToString(Request request) {
        StringBuilder sb = new StringBuilder();
        for (String param : request.getParams()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(param);
        }
        return sb.toString();
    }
}
