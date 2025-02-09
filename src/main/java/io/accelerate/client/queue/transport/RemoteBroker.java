package io.accelerate.client.queue.transport;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.LoggerFactory;
import io.accelerate.client.queue.serialization.DeserializationException;
import io.accelerate.client.queue.abstractions.Request;
import io.accelerate.client.queue.abstractions.response.Response;
import io.accelerate.client.queue.serialization.JsonRpcSerializationProvider;
import io.accelerate.client.queue.serialization.SerializationProvider;

import javax.jms.*;
import java.util.Optional;

public class RemoteBroker implements AutoCloseable {
    private final Connection connection;


    private final Session session;
    private final MessageConsumer messageConsumer;
    private final MessageProducer messageProducer;
    private final int requestTimeoutMillis;

    private SerializationProvider serializationProvider;

    public RemoteBroker(String hostname,
                        int port,
                        int requestTimeoutMillis,
                        String requestQueue,
                        String responseQueue) throws JMSException {
        String brokerURL = String.format("tcp://%s:%s", hostname, port);
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerURL);
        connection = connectionFactory.createConnection();

        LoggerFactory.getLogger(RemoteBroker.class).debug("Connecting to the remote broker");
        connection.start();
        session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

        messageConsumer = session.createConsumer(session.createQueue(requestQueue));

        messageProducer = session.createProducer(session.createQueue(responseQueue));
        messageProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        this.requestTimeoutMillis = requestTimeoutMillis;
        serializationProvider = new JsonRpcSerializationProvider();
    }

    public Optional<Request> receive() throws BrokerCommunicationException {
        try {
            //Debt: We should have no timeout. This method could exit if we put a special close message in the queue
            StringMessage messageText = new StringMessage(messageConsumer.receive(requestTimeoutMillis));
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
