package competition.client.serialization;

import competition.client.abstractions.Request;
import competition.client.abstractions.Response;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public interface SerializationProvider {

    Request deserialize(String messageText);

    String serialize(Response response);
}
