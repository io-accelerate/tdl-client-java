package competition.client.respond;

import competition.client.abstractions.Request;
import competition.client.abstractions.Response;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class ValidateResponse implements ResponseStrategy {
    private final ResponseStrategy wrappedStrategy;

    public ValidateResponse(ResponseStrategy wrappedStrategy) {
        this.wrappedStrategy = wrappedStrategy;
    }

    @Override
    public Response respondTo(Request request) {
        Response response = wrappedStrategy.respondTo(request);

        if (response.getResult() == null) {
            response = null;
        }

        return response;
    }
}
