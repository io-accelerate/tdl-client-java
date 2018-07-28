package utils.jmx.broker;


import com.google.gson.JsonElement;

import java.util.*;


/**
 * Created by julianghionoiu on 13/06/2015.
 */
class Playground {


    public static void main(String[] args) throws Exception {
        JolokiaSession jolokiaSession = JolokiaSession.connect("localhost", 28161);


        {
            Map<String, Object> attribute = new HashMap<>();
            attribute.put("type", "read");
            attribute.put("mbean", "org.apache.activemq:type=Broker,brokerName=TEST.BROKER,destinationType=Queue,destinationName=test.req");
            attribute.put("attribute", "QueueSize");
            JsonElement response = jolokiaSession.request(attribute);
            System.out.println(response.getAsInt());
        }

        {
            Map<String, Object> operation = new HashMap<>();
            operation.put("type", "exec");
            operation.put("mbean", "org.apache.activemq:type=Broker,brokerName=TEST.BROKER");
            operation.put("operation", "addQueue");
            operation.put("arguments", list("test.req"));
            jolokiaSession.request(operation);
        }

        {
            Map<String, Object> operation = new HashMap<>();
            operation.put("type", "exec");
            operation.put("mbean", "org.apache.activemq:type=Broker,brokerName=TEST.BROKER,destinationType=Queue,destinationName=test.req");
            operation.put("operation", "sendTextMessage(java.lang.String)");
            operation.put("arguments", list("test message"));
            jolokiaSession.request(operation);
        }

        {
            Map<String, Object> operation = new HashMap<>();
            operation.put("type", "exec");
            operation.put("mbean", "org.apache.activemq:type=Broker,brokerName=TEST.BROKER,destinationType=Queue,destinationName=test.req");
            operation.put("operation", "browse()");
            JsonElement request = jolokiaSession.request(operation);

            List<String> messageContents = new ArrayList<>();
            request.getAsJsonArray().forEach(jsonElement -> {
                String text = jsonElement.getAsJsonObject().getAsJsonPrimitive("Text").getAsString();
                messageContents.add(text);
            });
            System.out.println(messageContents);
        }

        {
            Map<String, Object> operation = new HashMap<>();
            operation.put("type", "exec");
            operation.put("mbean", "org.apache.activemq:type=Broker,brokerName=TEST.BROKER,destinationType=Queue,destinationName=test.req");
            operation.put("operation", "purge()");
            jolokiaSession.request(operation);
        }

    }

    private static List<String> list(String ... strings) {
        return Arrays.asList(strings);
    }
}
