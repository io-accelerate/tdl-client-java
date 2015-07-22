package competition.client.respond;

import competition.client.abstractions.Request;
import competition.client.abstractions.Response;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
@FunctionalInterface
public interface ResponseStrategy {
    Response respondTo(Request request);
}
