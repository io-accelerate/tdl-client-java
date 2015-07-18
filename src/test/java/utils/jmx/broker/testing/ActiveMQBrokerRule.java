package utils.jmx.broker.testing;

import org.junit.rules.ExternalResource;
import utils.jmx.broker.RemoteJmxBroker;
import utils.jmx.broker.RemoteJmxQueue;

/**
 * Created by julianghionoiu on 13/06/2015.
 */
public class ActiveMQBrokerRule  extends ExternalResource {
    private final String hostname;
    private final Integer port;
    private final String brokerName;

    private RemoteJmxBroker remoteJmxBroker;

    public ActiveMQBrokerRule(String hostname, Integer port, String brokerName) {
        this.hostname = hostname;
        this.port = port;
        this.brokerName = brokerName;
    }


    @Override
    protected void before() throws Throwable {
        remoteJmxBroker = RemoteJmxBroker.connect(hostname, port, brokerName);
    }

    //~~~~ Facade to broker

    public RemoteJmxQueue addQueue(String queueName) throws Exception {
        return remoteJmxBroker.addQueue(queueName);
    }
}
