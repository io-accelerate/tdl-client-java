package competition;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class Client {
    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);
    private final String brokerURL;
    private final String username;

    private static final long REQUEST_TIMEOUT = 1000L;

    public Client(String brokerURL, String username) {
        this.brokerURL = brokerURL;
        this.username = username;
    }

    //Design: A factory will be useful

    public void goLiveWith(RequestListener requestListener) {
        //Debt: Should add a unit test for this entire library
        String requestQueue = username +".req";
        String responseQueue = username +".resp";

        //Design: serializedParam can become Request and the returned object could be called Response

        //Design: this whole code can be abstracted into something
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerURL);
        Connection connection;
        try {
            connection = connectionFactory.createConnection();
            connection.start();
            Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

            //Create the consumer
            MessageConsumer responseConsumer = session.createConsumer(session.createQueue(requestQueue));

            //Create the producer
            CompetitionMessageProducer messageProducer = CompetitionMessageProducer.createForQueue(responseQueue, session);

            //This class will handle the incoming messages
            CompetitionMessageListener competitionMessageListener = new CompetitionMessageListener(requestListener, messageProducer);

            //Design: The wait time should be bigger in order to handle network delays
            Message message = responseConsumer.receive(REQUEST_TIMEOUT);
            while (message != null) {
                //Design: This method could exit if we put a special close message to the queue
                competitionMessageListener.onMessage(message);
                message.acknowledge();
                message = responseConsumer.receive(REQUEST_TIMEOUT);
            }

            //TODO: Transform these into a closable object
            responseConsumer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            LOGGER.error("Problem communicating with the broker", e);
        }
    }

    public void trialRunWith() {

    }

    private static class CompetitionMessageProducer {
        private final Session session;
        private final MessageProducer producer;

        //~~~ Factories

        public CompetitionMessageProducer(Session session, MessageProducer producer) {
            this.session = session;
            this.producer = producer;
        }

        public static CompetitionMessageProducer createForQueue(String queueName, Session session) throws JMSException {
            MessageProducer producer =  session.createProducer(session.createQueue(queueName));
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            return new CompetitionMessageProducer(session, producer);
        }

        //~~~ Actions

        public void send(String content) throws JMSException {
            TextMessage txtMessage = session.createTextMessage();
            txtMessage.setText(content);
            producer.send(txtMessage);
        }
    }

    private static class CompetitionMessageListener implements MessageListener {
        private final RequestListener requestListener;
        private final CompetitionMessageProducer producer;

        public CompetitionMessageListener(RequestListener requestListener, CompetitionMessageProducer producer) {
            this.requestListener = requestListener;
            this.producer = producer;
        }

        @Override
        public void onMessage(Message message) {
            try {
                //Debt: The serialization strategy should be revisited. It has to use standard protocols.
                String messageText = "";
                if (message instanceof TextMessage) {
                    TextMessage textMessage = (TextMessage) message;
                    messageText = textMessage.getText();
                }
                if (message instanceof BytesMessage) {
                    BytesMessage bytesMessage = (BytesMessage) message;
                    byte[] body = new byte[(int)bytesMessage.getBodyLength()];
                    bytesMessage.readBytes(body, (int) bytesMessage.getBodyLength());
                    messageText = new String (body);
                }

                //Deserialize
                String[] items = messageText.split(", ");
                String requestId = items[0];
                String serializedParam = items[1];

                //Compute
                Object response = requestListener.onRequest(serializedParam);

                //Serialize
                String serializedResponse = response.toString();

                //Serialize and respond
                producer.send(requestId + ", " + serializedResponse);
                System.out.println("id = " + requestId + ", req = " + serializedParam + ", resp: " + serializedResponse);
                message.acknowledge();
            } catch (JMSException e) {
                //DEBT: Fix logging (no binding found)
                LoggerFactory.getLogger(CompetitionMessageListener.class).error("Error sending response",e);
            }
        }
    }

    public interface RequestListener {
        Object onRequest(String serializedParam);
    }
}
