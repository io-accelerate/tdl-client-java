package audit;

import com.google.common.primitives.Floats;

/**
 * Created by julianghionoiu on 03/02/2016.
 */
public final class PresentationUtils {

    private PresentationUtils() {
        //Utility class
    }

    public static String toDisplayableString(Object[] items) {
        StringBuilder sb = new StringBuilder();
        for (Object item : items) {
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

        if (isNotNumber(item)) {
            representation = addQuotes(representation);
        }

        return representation;
    }

    //~~~ Handle individual item

    private static String asString(Object item) {
        return ""+item;
    }

    private static boolean isMultilineString(String representation) {
        return representation.contains("\n");
    }

    private static boolean isNotNumber(Object item) {
        return !isNumber(item);
    }

    private static boolean isNumber(Object item) {
        boolean isNumber = false;
        if (item instanceof Number) {
            isNumber = true;
        } else
        if (item instanceof String) {
            Float numberRepresentation = Floats.tryParse((String) item);
            isNumber = numberRepresentation != null;
        }

        return isNumber;
    }

    private static String suppressExtraLines(String representation) {
        String[] parts = representation.split("\n");
        representation = parts[0];

        int suppressedParts = parts.length - 1;
        representation += " .. ( "+ suppressedParts +" more line";

        if (suppressedParts > 1) {
            representation += "s";
        }

        representation += " )";
        return representation;
    }

    private static String addQuotes(String representation) {
        return "\""+representation+"\"";
    }

}
