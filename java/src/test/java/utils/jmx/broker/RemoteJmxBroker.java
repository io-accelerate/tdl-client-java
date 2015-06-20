package utils.jmx.broker;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;

/**
 * Created by julianghionoiu on 13/06/2015.
 */
public class RemoteJmxBroker {
    private final MBeanServerConnection jmxSession;
    private final String brokerName;

    private RemoteJmxBroker(MBeanServerConnection jmxSession, String brokerName) {
        this.jmxSession = jmxSession;
        this.brokerName = brokerName;
    }

    public static RemoteJmxBroker connect(String hostname, Integer port, String brokerName) throws IOException {
        String connectionString = String.format("service:jmx:rmi:///jndi/rmi://%s:%s/jmxrmi", hostname, port);
        JMXServiceURL url = new JMXServiceURL(connectionString);
        JMXConnector factory = JMXConnectorFactory.connect(url, null);
        MBeanServerConnection beanServerConnection =
                factory.getMBeanServerConnection();
        return new RemoteJmxBroker(beanServerConnection, brokerName);
    }

    //~~~~ Queue management

    public RemoteJmxQueue getQueue(String queueName) {
        return new RemoteJmxQueue(jmxSession, brokerName, queueName);
    }

    public RemoteJmxQueue addQueue(String queueName) throws Exception {
        jmxSession.invoke(JmxUtils.asBroker(brokerName),
                "addQueue", JmxUtils.params(queueName), JmxUtils.types(String.class.getName()));
        return getQueue(queueName);
    }

    public void removeQueue(String queueName) throws Exception {
        jmxSession.invoke(JmxUtils.asBroker(brokerName),
                "removeQueue", JmxUtils.params(queueName), JmxUtils.types(String.class.getName()));
    }
}
