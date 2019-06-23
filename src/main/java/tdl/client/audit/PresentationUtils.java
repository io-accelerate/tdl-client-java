package tdl.client.audit;

import com.google.gson.JsonElement;

import java.util.List;

/**
 * Created by julianghionoiu on 03/02/2016.
 */
public final class PresentationUtils {

    private PresentationUtils() {
        //Utility class
    }

    public static String toDisplayableRequest(List<JsonElement> items) {
        StringBuilder sb = new StringBuilder();
        for (JsonElement item : items) {
            if (sb.length() > 0) {
                sb.append(", ");
            }

            String representation;

            if (item.isJsonArray()) {
                representation = item.toString();
                representation = representation.replaceAll(",", ", ");
            } else {
                representation = item.toString();

                if (isMultilineString(representation)) {
                    representation = suppressExtraLines(representation);
                }
            }

            sb.append(representation);
        }
        return sb.toString();
    }

    public static String toDisplayableResponse(Object item) {
        if (item == null) {
            return "null";
        }

        String representation = item.toString();

        if (isMultilineString(representation)) {
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
