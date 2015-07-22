package competition.client;

import competition.client.abstractions.Request;
import competition.client.abstractions.Response;
import competition.client.abstractions.UserImplementation;
import competition.client.respond.AuditTraffic;
import competition.client.respond.ObtainResponse;
import competition.client.respond.ResponseStrategy;
import competition.client.respond.ValidateResponse;
import competition.client.serialization.CsvSerializationProvider;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class DeserializeAndRespondToMessage implements MessageHandler {
    private final CsvSerializationProvider serializationProvider;
    private final ResponseStrategy responseStrategy;

    public DeserializeAndRespondToMessage(UserImplementation userImplementation) {
        this.serializationProvider = new CsvSerializationProvider();
        this.responseStrategy = new ValidateResponse(new AuditTraffic(new ObtainResponse(userImplementation)));
    }

    public static DeserializeAndRespondToMessage using(UserImplementation userImplementation) {
        return new DeserializeAndRespondToMessage(userImplementation);
    }

    public String respondTo(String messageText) {
        Request request = serializationProvider.deserialize(messageText);
        Response response = responseStrategy.respondTo(request);
        return serializationProvider.serialize(response);
    }
}
