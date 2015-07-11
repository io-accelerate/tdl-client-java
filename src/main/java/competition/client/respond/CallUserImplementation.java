package competition.client.respond;

import competition.client.DeserializeAndProcessMessage;
import competition.client.abstractions.Request;
import competition.client.abstractions.Response;
import competition.client.abstractions.UserImplementation;
import org.slf4j.LoggerFactory;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class CallUserImplementation implements RespondToRequest {
    private final UserImplementation userImplementation;

    public CallUserImplementation(UserImplementation userImplementation) {
        this.userImplementation = userImplementation;
    }

    @Override
    public Response respondTo(Request request) {
        Object result = null;
        try {
            result = userImplementation.process(request.getParams());
        } catch (Exception e) {
            LoggerFactory.getLogger(DeserializeAndProcessMessage.class)
                    .info("The user implementation has thrown exception.", e);
        }

        if (result == null) {
            LoggerFactory.getLogger(DeserializeAndProcessMessage.class)
                    .info("User implementation has returned \"null\".");
        }

        return new Response(request.getRequestId(), result);
    }
}
