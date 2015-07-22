package competition.client.transport;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.LoggerFactory;

import javax.jms.*;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class RemoteBroker implements AutoCloseable {
    private static final long REQUEST_TIMEOUT = 1000L;

    private final Connection connection;
    private final Session session;
    private final MessageConsumer messageConsumer;
    private final MessageProducer messageProducer;

    public RemoteBroker(String hostname, int port, String username) throws JMSException {
        String brokerURL = String.format("tcp://%s:%s", hostname, port);
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerURL);
        connection = connectionFactory.createConnection();

        LoggerFactory.getLogger(RemoteBroker.class).info("Starting client");
        connection.start();
        session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

        String requestQueue = username + ".req";
        messageConsumer = session.createConsumer(session.createQueue(requestQueue));

        String responseQueue = username + ".resp";
        messageProducer = session.createProducer(session.createQueue(responseQueue));
        messageProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
    }


    public StringMessage receive() throws JMSException {
        //Obs: We should have no timeout
        return new StringMessage(messageConsumer.receive(REQUEST_TIMEOUT));
    }

    public void send(String content) throws JMSException {
        TextMessage txtMessage = session.createTextMessage();
        txtMessage.setText(content);
        messageProducer.send(txtMessage);
    }

    @Override
    public void close() throws Exception {
        LoggerFactory.getLogger(RemoteBroker.class).info("Stopping client");
        connection.close();
    }
}
