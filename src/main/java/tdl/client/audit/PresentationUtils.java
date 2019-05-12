package tdl.client.audit;

import com.google.gson.JsonElement;

/**
 * Created by julianghionoiu on 03/02/2016.
 */
public final class PresentationUtils {

    private PresentationUtils() {
        //Utility class
    }

    public static String toDisplayableString(JsonElement[] items) {
        StringBuilder sb = new StringBuilder();
        for (JsonElement item : items) {
            if (sb.length() > 0) {
                sb.append(", ");
            }

            sb.append(toDisplayableString(item));
        }
        return sb.toString();
    }

    public static String toDisplayableString(Object item) {
        String representation = asString(item);

        if (isMultilineString(representation)) {
            representation = suppressExtraLines(representation);
        }

        return representation;
    }

    //~~~ Handle individual item

    private static String asString(Object item) {
        return ""+item;
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
