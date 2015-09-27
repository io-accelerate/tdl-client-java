package utils.jmx.broker;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by julianghionoiu on 13/06/2015.
 */
public class RemoteJmxBroker {
    private final JolokiaSession jolokiaSession;
    private final String brokerName;

    private RemoteJmxBroker(JolokiaSession jolokiaSession, String brokerName) {
        this.jolokiaSession = jolokiaSession;
        this.brokerName = brokerName;
    }

    public static RemoteJmxBroker connect(String hostname, Integer port, String brokerName) throws Exception {
        try {
            JolokiaSession jolokiaSession = JolokiaSession.connect(hostname, port);
            return new RemoteJmxBroker(jolokiaSession, brokerName);
        } catch (Exception e) {
            throw new Exception("Broker is busted.", e);
        }
    }

    //~~~~ Queue management

    public RemoteJmxQueue addQueue(String queueName) throws Exception {
        Map<String, Object> operation = new HashMap<>();
        operation.put("type", "exec");
        operation.put("mbean", "org.apache.activemq:type=Broker,brokerName=TEST.BROKER");
        operation.put("operation", "addQueue");
        operation.put("arguments", Collections.singletonList("test.req"));
        jolokiaSession.request(operation);
        return new RemoteJmxQueue(jolokiaSession, brokerName, queueName);
    }
}
