package tdl.client;

import tdl.client.abstractions.ImplementationMap;
import tdl.client.abstractions.Request;
import tdl.client.abstractions.Response;
import tdl.client.abstractions.UserImplementation;
import tdl.client.audit.AuditStream;
import tdl.client.respond.AuditTraffic;
import tdl.client.respond.ObtainResponse;
import tdl.client.respond.ResponseStrategy;
import tdl.client.respond.ValidateResponse;
import tdl.client.serialization.CsvSerializationProvider;
import tdl.client.serialization.JsonRpcSerializationProvider;
import tdl.client.serialization.SerializationProvider;

import java.io.PrintStream;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class DeserializeAndRespondToMessage implements MessageHandler {
    private final SerializationProvider serializationProvider;
    private final ResponseStrategy responseStrategy;

    public DeserializeAndRespondToMessage(ImplementationMap implementationMap, AuditStream auditStream) {
        this.serializationProvider = new JsonRpcSerializationProvider();
        this.responseStrategy = new ValidateResponse(new AuditTraffic(auditStream, new ObtainResponse(implementationMap)));
    }

    public static DeserializeAndRespondToMessage using(ImplementationMap implementationMap, AuditStream auditStream) {
        return new DeserializeAndRespondToMessage(implementationMap, auditStream);
    }

    public String respondTo(String messageText) {
        Request request = serializationProvider.deserialize(messageText);
        Response response = responseStrategy.respondTo(request);
        return serializationProvider.serialize(response);
    }
}
