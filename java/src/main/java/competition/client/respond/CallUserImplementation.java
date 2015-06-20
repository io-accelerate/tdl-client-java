package competition.client.respond;

import competition.client.DeserializeAndRespondToMessage;
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
            LoggerFactory.getLogger(DeserializeAndRespondToMessage.class)
                    .info("The user implementation has thrown exception.", e);
        }

        if (result == null) {
            LoggerFactory.getLogger(DeserializeAndRespondToMessage.class)
                    .info("User implementation has returned \"null\".");
        }

        return new Response(request.getRequestId(), result);
    }
}
