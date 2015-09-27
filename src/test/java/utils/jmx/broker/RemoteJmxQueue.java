package utils.jmx.broker;

import com.google.gson.JsonElement;

import javax.management.MBeanServerConnection;
import java.util.*;

/**
 * Created by julianghionoiu on 13/06/2015.
 */
public class RemoteJmxQueue {
    private final JolokiaSession jolokiaSession;
    private final String queueBean;

    public RemoteJmxQueue(JolokiaSession jolokiaSession, String brokerName, String queueName) {
        this.jolokiaSession = jolokiaSession;
        this.queueBean = String.format("org.apache.activemq:type=Broker,brokerName=%s,destinationType=Queue,destinationName=%s",
                brokerName, queueName);
    }

    //~~~~ Queue operations

    public void sendTextMessage(String message) throws Exception {
        Map<String, Object> operation = new HashMap<>();
        operation.put("type", "exec");
        operation.put("mbean", queueBean);
        operation.put("operation", "sendTextMessage(java.lang.String)");
        operation.put("arguments", Collections.singletonList(message));
        jolokiaSession.request(operation);
    }

    public Long getSize() throws Exception {
        Map<String, Object> attribute = new HashMap<>();
        attribute.put("type", "read");
        attribute.put("mbean", queueBean);
        attribute.put("attribute", "QueueSize");
        JsonElement response = jolokiaSession.request(attribute);
        return response.getAsLong();
    }

    public List<String> getMessageContents() throws Exception {
        Map<String, Object> operation = new HashMap<>();
        operation.put("type", "exec");
        operation.put("mbean", queueBean);
        operation.put("operation", "browse()");
        JsonElement request = jolokiaSession.request(operation);

        List<String> messageContents = new ArrayList<>();
        request.getAsJsonArray().forEach(jsonElement -> {
            String text = jsonElement.getAsJsonObject().getAsJsonPrimitive("Text").getAsString();
            messageContents.add(text);
        });
        return messageContents;
    }

    public void purge() throws Exception {
        Map<String, Object> operation = new HashMap<>();
        operation.put("type", "exec");
        operation.put("mbean", queueBean);
        operation.put("operation", "purge()");
        jolokiaSession.request(operation);
    }
}
