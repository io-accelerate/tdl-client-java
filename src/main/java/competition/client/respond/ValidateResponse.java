package competition.client.respond;

import competition.client.abstractions.Request;
import competition.client.abstractions.Response;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class ValidateResponse implements RespondToRequest {
    private final RespondToRequest wrappedObject;

    public ValidateResponse(RespondToRequest wrappedObject) {
        this.wrappedObject = wrappedObject;
    }

    @Override
    public Response respondTo(Request request) {
        Response response = wrappedObject.respondTo(request);

        if (response.getResult() == null) {
            response = null;
        }

        return response;
    }
}
