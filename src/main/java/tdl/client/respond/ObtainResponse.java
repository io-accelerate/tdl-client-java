package tdl.client.respond;

import tdl.client.DeserializeAndRespondToMessage;
import tdl.client.abstractions.ImplementationMap;
import tdl.client.abstractions.Request;
import tdl.client.abstractions.Response;
import tdl.client.abstractions.UserImplementation;
import org.slf4j.LoggerFactory;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class ObtainResponse implements ResponseStrategy {
    private final ImplementationMap implementationMap;

    public ObtainResponse(ImplementationMap implementationMap) {
        this.implementationMap = implementationMap;
    }

    @Override
    public Response respondTo(Request request) {
        Object result = null;
        try {
            String methodName = request.getMethodName();
            UserImplementation userImplementation = implementationMap.getImplementationFor(methodName);

            result = userImplementation.process(request.getParams());
        } catch (Exception e) {
            LoggerFactory.getLogger(DeserializeAndRespondToMessage.class)
                    .warn("The user implementation has thrown exception.", e);
        }

        if (result == null) {
            LoggerFactory.getLogger(DeserializeAndRespondToMessage.class)
                    .warn("User implementation has returned \"null\".");
        }

        return new Response(request.getId(), result);
    }
}
