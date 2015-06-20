package competition.client;

import competition.client.abstractions.Request;
import competition.client.abstractions.Response;
import competition.client.abstractions.UserImplementation;
import competition.client.respond.AuditTraffic;
import competition.client.respond.CallUserImplementation;
import competition.client.respond.RespondToRequest;
import competition.client.respond.ValidateResponse;
import competition.client.serialization.CsvSerializationProvider;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class DeserializeAndProcessMessage {
    private final CsvSerializationProvider serializationProvider;
    private final RespondToRequest responseStrategy;

    public DeserializeAndProcessMessage(UserImplementation userImplementation) {
        this.serializationProvider = new CsvSerializationProvider();
        this.responseStrategy = new ValidateResponse(new AuditTraffic(new CallUserImplementation(userImplementation)));
    }

    public String onRequest(String messageText) {
        Request request = serializationProvider.deserialize(messageText);
        Response response = responseStrategy.respondTo(request);
        return serializationProvider.serialize(response);
    }
}
