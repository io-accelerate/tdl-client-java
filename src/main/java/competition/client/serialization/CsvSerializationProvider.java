package competition.client.serialization;

import competition.client.abstractions.Request;
import competition.client.abstractions.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class CsvSerializationProvider implements SerializationProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(CsvSerializationProvider.class);

    @Override
    public Request deserialize(String messageText) {
        String[] items = messageText.split(", ", 2);
        LOGGER.debug("Received items: " + Arrays.toString(items));

        String requestId = items[0];
        String serializedParams = items[1];
        String[] params = serializedParams.split(", ");

        return new Request(requestId, params);
    }


    @Override
    public String serialize(Response response) {
        String serializedResponse = null;
        if (response != null) {
            serializedResponse = response.getId() + ", " + response.getResult();
        }

        return serializedResponse;
    }
}
