package tdl.client;

import utils.jmx.broker.RemoteJmxBroker;
import utils.jmx.broker.RemoteJmxQueue;

/**
 * Created by julianghionoiu on 11/10/2015.
 */
public class SingletonTestBroker {
    // Test broker admin connection
    private static final String HOSTNAME = "localhost";
    private static final int JMX_PORT = 28161;
    private static final String BROKER_NAME = "TEST.BROKER";

    private static RemoteJmxBroker BROKER_INSTANCE;

    public SingletonTestBroker() throws Exception {
        //All the instances are just a proxy for the same broker
        if (BROKER_INSTANCE == null) {
            BROKER_INSTANCE = RemoteJmxBroker.connect(HOSTNAME, JMX_PORT, BROKER_NAME);
        }
    }

    public RemoteJmxQueue addQueue(String queueName) throws Exception {
        return BROKER_INSTANCE.addQueue(queueName);
    }
}
