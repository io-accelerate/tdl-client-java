package tdl.client.serialization;

import tdl.client.abstractions.Request;
import tdl.client.abstractions.Response;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public interface SerializationProvider {

    Request deserialize(String messageText);

    String serialize(Response response);
}
