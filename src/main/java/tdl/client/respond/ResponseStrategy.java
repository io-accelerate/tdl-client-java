package tdl.client.respond;

import tdl.client.abstractions.Request;
import tdl.client.abstractions.Response;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
@FunctionalInterface
public interface ResponseStrategy {
    Response respondTo(Request request);
}
