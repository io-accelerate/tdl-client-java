package broker.jmx;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * Created by julianghionoiu on 13/06/2015.
 */
public class JmxUtils {

    public static final Object[] params(Object... objects) {
        return objects;
    }

    public static final String[] types(String... signature) {
        return signature;
    }

    public static final ObjectName asBroker(String brokerName) {
        try {
            String ref = String.format("org.apache.activemq:type=Broker,brokerName=%s", brokerName);
            return new ObjectName(ref);
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException("The object name has wrong format", e);
        }
    }

    public static final ObjectName asQueue(String brokerName, String queueName) {
        try {
            String ref = String.format("org.apache.activemq:type=Broker,brokerName=%s,destinationType=Queue,destinationName=%s",
                    brokerName, queueName);
            return new ObjectName(ref);
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException("The object name has wrong format", e);
        }
    }
}
