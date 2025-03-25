package io.accelerate.client.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.accelerate.client.queue.abstractions.ParamAccessor;

import java.util.List;

/**
 * Created by julianghionoiu on 03/02/2016.
 */
public final class PresentationUtils {

    private final ObjectMapper objectMapper;

    public PresentationUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public  String toDisplayableRequest(List<ParamAccessor> items) {
        StringBuilder sb = new StringBuilder();
        for (ParamAccessor item : items) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(serialiseAndCompress(item.getAsObject(Object.class)));
        }
        return sb.toString();
    }

    public String toDisplayableResponse(Object item) {
        return serialiseAndCompress(item);
    }

    private String serialiseAndCompress(Object item) {
        String representation;
        try {
            representation = objectMapper.writeValueAsString(item);
        } catch (JsonProcessingException e) {
            representation = "serializationError";
        }

        if (isList(item)) {
            representation = representation.replaceAll(",", ", ");
        } else if (isMultilineString(representation)) {
            representation = suppressExtraLines(representation);
        }
        return representation;
    }
    
    private static boolean isList(Object item) {
        return item instanceof List;
    }

    private static boolean isMultilineString(String representation) {
        return representation.contains("\\n");
    }

    private static String suppressExtraLines(String representation) {
        String[] parts = representation.split("\\\\n");
        representation = parts[0];

        int suppressedParts = parts.length - 1;
        representation += " .. ( "+ suppressedParts +" more line";

        if (suppressedParts > 1) {
            representation += "s";
        }

        representation += " )\"";
        return representation;
    }

}
