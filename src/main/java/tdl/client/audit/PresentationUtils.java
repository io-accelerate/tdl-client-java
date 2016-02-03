package tdl.client.audit;

/**
 * Created by julianghionoiu on 03/02/2016.
 */
public final class PresentationUtils {

    private PresentationUtils() {
        //Utility class
    }

    public static String toDisplayableString(Object ... items) {
        StringBuilder sb = new StringBuilder();
        for (Object item : items) {
            if (sb.length() > 0) {
                sb.append(", ");
            }


            String representation = ""+item;

            if (representation.contains("\n")) {
                String[] parts = representation.split("\n");
                representation = parts[0];

                int suppressedParts = parts.length - 1;
                representation += " .. ( "+ suppressedParts +" more line";

                if (suppressedParts > 1) {
                    representation += "s";
                }

                representation += " )";
            }

            if (item instanceof Number) {
                sb.append(representation);
            } else {
                sb.append("\"").append(representation).append("\"");
            }
        }
        return sb.toString();
    }

}
