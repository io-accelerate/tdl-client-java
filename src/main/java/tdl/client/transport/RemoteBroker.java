package tdl.client.transport;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.LoggerFactory;
import tdl.client.abstractions.Request;
import tdl.client.abstractions.Response;
import tdl.client.serialization.JsonRpcSerializationProvider;
import tdl.client.serialization.SerializationProvider;

import javax.jms.*;
import java.util.Optional;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class RemoteBroker implements AutoCloseable {
    private static final long REQUEST_TIMEOUT = 1000L;

    private final Connection connection;
    private final Session session;
    private final MessageConsumer messageConsumer;
    private final MessageProducer messageProducer;

    private SerializationProvider serializationProvider;

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

        serializationProvider = new JsonRpcSerializationProvider();
    }

    public Optional<Request> receive() throws JMSException {
        //Debt: We should have no timeout. This method could exit if we put a special close message in the queue
        StringMessage messageText = new StringMessage(messageConsumer.receive(REQUEST_TIMEOUT));
        return serializationProvider.deserialize(messageText);
    }

    public void respondTo(Request request, Response response) throws JMSException {
        String serializedResponse = serializationProvider.serialize(response);

        TextMessage txtMessage = session.createTextMessage();
        txtMessage.setText(serializedResponse);
        messageProducer.send(txtMessage);

        request.getOriginalMessage().acknowledge();
    }

    @Override
    public void close() throws Exception {
        LoggerFactory.getLogger(RemoteBroker.class).info("Stopping client");
        session.close();
        connection.close();
    }
}
