package utils.jmx.broker;

import javax.management.MBeanServerConnection;
import javax.management.openmbean.CompositeData;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by julianghionoiu on 13/06/2015.
 */
public class RemoteJmxQueue {
    private final MBeanServerConnection jmxSession;
    private final String brokerName;
    private final String queueName;

    public RemoteJmxQueue(MBeanServerConnection jmxSession, String brokerName, String queueName) {
        this.jmxSession = jmxSession;
        this.brokerName = brokerName;
        this.queueName = queueName;
    }

    //~~~~ Queue operations

    public void sendTextMessage(String message) throws Exception {
        jmxSession.invoke(JmxUtils.asQueue(brokerName, queueName),
                "sendTextMessage", JmxUtils.params(message), JmxUtils.types(String.class.getName()));
    }

    public Long getSize() throws Exception {
        return (Long) jmxSession.getAttribute(JmxUtils.asQueue(brokerName, queueName),
                "QueueSize");
    }

    public List<String> getMessageContents() throws Exception {
        CompositeData[] messages = (CompositeData[]) jmxSession.invoke(JmxUtils.asQueue(brokerName, queueName),
                "browse", JmxUtils.params(), JmxUtils.types());
        return Arrays.stream(messages)
                .map(compositeData -> (String) compositeData.get("Text"))
                .collect(Collectors.toList());
    }

    public void purge() throws Exception {
        jmxSession.invoke(JmxUtils.asQueue(brokerName, queueName),
                "purge", JmxUtils.params(), JmxUtils.types());
    }
}
