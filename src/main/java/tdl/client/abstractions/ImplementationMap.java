package tdl.client.abstractions;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by julianghionoiu on 17/10/2015.
 */
public class ImplementationMap {
    private final Map<String, UserImplementation> implementations;

    public ImplementationMap() {
        implementations = new HashMap<>();
    }

    public void register(String methodName, UserImplementation userImplementation) {
        implementations.put(methodName, userImplementation);
    }

    public UserImplementation getImplementationFor(String methodName) {
        if (implementations.containsKey(methodName)) {
            return implementations.get(methodName);
        } else {
            throw new NoSuchMethodError(String.format("Method \"%s\" is not registered in the implementation map.", methodName));
        }
    }
}
