package tdl.client.transport;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.LoggerFactory;
import tdl.client.serialization.DeserializationException;
import tdl.client.abstractions.Request;
import tdl.client.abstractions.response.Response;
import tdl.client.serialization.JsonRpcSerializationProvider;
import tdl.client.serialization.SerializationProvider;

import javax.jms.*;
import java.util.Optional;

public class RemoteBroker implements AutoCloseable {
    private final Connection connection;


    private final Session session;
    private final MessageConsumer messageConsumer;
    private final MessageProducer messageProducer;
    private final long timeToWaitForRequests;

    private SerializationProvider serializationProvider;

    public RemoteBroker(String hostname, int port, String uniqueId, long timeToWaitForRequests) throws JMSException {
        String brokerURL = String.format("tcp://%s:%s", hostname, port);
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerURL);
        connection = connectionFactory.createConnection();

        LoggerFactory.getLogger(RemoteBroker.class).debug("Connecting to the remote broker");
        connection.start();
        session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

        String requestQueue = uniqueId + ".req";
        messageConsumer = session.createConsumer(session.createQueue(requestQueue));

        String responseQueue = uniqueId + ".resp";
        messageProducer = session.createProducer(session.createQueue(responseQueue));
        messageProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        this.timeToWaitForRequests = timeToWaitForRequests;
        serializationProvider = new JsonRpcSerializationProvider();
    }

    public Optional<Request> receive() throws BrokerCommunicationException {
        try {
            //Debt: We should have no timeout. This method could exit if we put a special close message in the queue
            StringMessage messageText = new StringMessage(messageConsumer.receive(timeToWaitForRequests));
            return serializationProvider.deserialize(messageText);
        } catch (JMSException | DeserializationException e) {
            throw new BrokerCommunicationException(e);
        }
    }

    public void respondTo(Request request, Response response) throws BrokerCommunicationException {
        try {
            String serializedResponse = serializationProvider.serialize(response);

            TextMessage txtMessage = session.createTextMessage();
            txtMessage.setText(serializedResponse);
            messageProducer.send(txtMessage);

            request.getOriginalMessage().acknowledge();
        } catch (JMSException e) {
            throw new BrokerCommunicationException(e);
        }
    }

    @Override
    public void close() throws Exception {
        LoggerFactory.getLogger(RemoteBroker.class).debug("Stopping the connection to the broker");
        session.close();
        connection.close();
    }
}
