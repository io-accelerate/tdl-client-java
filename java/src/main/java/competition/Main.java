package competition;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.Map;

/**
 * Created by julianghionoiu on 11/06/2015.
 */
public class Main {


    // 1. The first start of the client should not consume any messages
    // 2. Returning null from business method should stop the processing
    // 3. Throwing messages from business method should stop the processing
    // 4. Returning a valid response should not consume messages if the user does not intend to go live
    // 5. The client should process all messages if the user decides to go live

    public static void main(String[] args) throws Exception {
        //Debt: Should add a unit test for this entire library
        String brokerURL = "tcp://localhost:61616";
        String requestQueue = "jgh.req";
        String responseQueue = "jgh.resp";

        //Design: serializedParam can become Request and the returned object could be called Response
        RequestListener requestListener = (String serializedParam) -> {
            Integer param = Integer.parseInt(serializedParam);
            return param + 1;
        };

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
            responseConsumer.setMessageListener(new CompetitionMessageListener(requestListener, messageProducer));
        } catch (JMSException e) {
            LoggerFactory.getLogger(Main.class).error("Problem communicating with the broker", e);
        }
    }

    private static class CompetitionMessageProducer {
        private final Session session;
        private final MessageProducer producer;


        public CompetitionMessageProducer(Session session, MessageProducer producer) {
            this.session = session;
            this.producer = producer;
        }

        public static CompetitionMessageProducer createForQueue(String queueName, Session session) throws JMSException {
            MessageProducer producer =  session.createProducer(session.createQueue(queueName));
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            return new CompetitionMessageProducer(session, producer);
        }

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

    private interface RequestListener {
        Object onRequest(String serializedParam);
    }
}
