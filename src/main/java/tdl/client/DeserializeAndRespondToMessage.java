package tdl.client;

import tdl.client.abstractions.Request;
import tdl.client.abstractions.Response;
import tdl.client.abstractions.UserImplementation;
import tdl.client.respond.AuditTraffic;
import tdl.client.respond.ObtainResponse;
import tdl.client.respond.ResponseStrategy;
import tdl.client.respond.ValidateResponse;
import tdl.client.serialization.CsvSerializationProvider;

import java.io.PrintStream;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class DeserializeAndRespondToMessage implements MessageHandler {
    private final CsvSerializationProvider serializationProvider;
    private final ResponseStrategy responseStrategy;

    public DeserializeAndRespondToMessage(UserImplementation userImplementation, PrintStream auditStream) {
        this.serializationProvider = new CsvSerializationProvider();
        this.responseStrategy = new ValidateResponse(new AuditTraffic(auditStream, new ObtainResponse(userImplementation)));
    }

    public static DeserializeAndRespondToMessage using(UserImplementation userImplementation, PrintStream printStream) {
        return new DeserializeAndRespondToMessage(userImplementation, printStream);
    }

    public String respondTo(String messageText) {
        Request request = serializationProvider.deserialize(messageText);
        Response response = responseStrategy.respondTo(request);
        return serializationProvider.serialize(response);
    }
}
