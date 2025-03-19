package io.accelerate.client.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.accelerate.client.queue.abstractions.ParamAccessor;

import java.util.List;

/**
 * Created by julianghionoiu on 03/02/2016.
 */
public final class PresentationUtils {
    
    public static String toDisplayableRequest(List<ParamAccessor> items) {
        ObjectMapper objectMapper = new ObjectMapper();
        StringBuilder sb = new StringBuilder();
        for (ParamAccessor item : items) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }

            String representation;
            try {
                representation = objectMapper.writeValueAsString(item.getAsObject(Object.class));
            } catch (JsonProcessingException e) {
                representation = "serializationError";
            }
            
            if (item.isArray()) {
                representation = representation.replaceAll(",", ", ");
            } else if (isMultilineString(representation)) {
                representation = suppressExtraLines(representation);
            }

            sb.append(representation);
        }
        return sb.toString();
    }

    public static String toDisplayableResponse(Object item) {
        ObjectMapper objectMapper = new ObjectMapper();
        if (item == null) {
            return "null";
        }

        String representation;
        try {
            representation = objectMapper.writeValueAsString(item);
        } catch (JsonProcessingException e) {
            representation = "serializationError";
        }

        if (item instanceof List) {
            representation = representation.replaceAll(",", ", ");
        } else if (isMultilineString(representation)) {
            representation = suppressExtraLines(representation);
        }

        return representation;
    }

    //~~~ Handle individual item

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
