package competition;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.Arrays;

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

    public void goLiveWith(RequestListener requestListener) {
        performMagic(requestListener, true);

    }

    public void trialRunWith(RequestListener requestListener) {
        performMagic(requestListener, false);
    }

    private void performMagic(RequestListener requestListener, boolean isGoLive) {
        String requestQueue = username +".req";
        String responseQueue = username +".resp";

        //Design: serializedParam can become Request and the returned object could be called Response

        //Design: this whole code can be abstracted into something
        try (CompetitionServerConnection connection = new CompetitionServerConnection(brokerURL, username)){
            CompetitionMessageListener competitionMessageListener = new CompetitionMessageListener(
                    requestListener, connection, isGoLive);

            //Design: The wait time should be bigger in order to handle network delays
            Message message = connection.receive(REQUEST_TIMEOUT);
            while (message != null) {
                //Design: This method could exit if we put a special close message to the queue
                boolean shouldContinue = competitionMessageListener.doMessage(message);

                if (shouldContinue && isGoLive) {
                    message = connection.receive(REQUEST_TIMEOUT);
                } else {
                    break;
                }
            }

            LoggerFactory.getLogger(Client.class).info("Stopping client.");
        } catch (Exception e) {
            LOGGER.error("Problem communicating with the broker", e);
        }
    }

    private static class CompetitionServerConnection implements AutoCloseable {
        private final Connection connection;
        private final Session session;
        private final MessageConsumer messageConsumer;
        private final MessageProducer messageProducer;

        public CompetitionServerConnection(String brokerURL, String username) throws JMSException {
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerURL);
            connection = connectionFactory.createConnection();

            LoggerFactory.getLogger(CompetitionServerConnection.class).info("Starting client");
            connection.start();
            session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

            String requestQueue = username +".req";
            messageConsumer = session.createConsumer(session.createQueue(requestQueue));

            String responseQueue = username +".resp";
            messageProducer = session.createProducer(session.createQueue(responseQueue));
            messageProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        }

        public Message receive(long timeout) throws JMSException {
            return messageConsumer.receive(timeout);
        }

        public void send(String content) throws JMSException {
            TextMessage txtMessage = session.createTextMessage();
            txtMessage.setText(content);
            messageProducer.send(txtMessage);
        }

        @Override
        public void close() throws Exception {
            LoggerFactory.getLogger(CompetitionServerConnection.class).info("Stopping client");
            connection.close();
        }
    }


    private static class CompetitionMessageListener {
        private final RequestListener requestListener;
        private final CompetitionServerConnection connection;
        private final boolean isGoLive;

        public CompetitionMessageListener(RequestListener requestListener, CompetitionServerConnection connection, boolean isGoLive) {
            this.requestListener = requestListener;
            this.connection = connection;
            this.isGoLive = isGoLive;
        }

        public boolean doMessage(Message message) {
            boolean shouldContinue = false;
            try {
                //Future: The serialization strategy should be revisited. It has to use standard protocols.
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
                String[] items = messageText.split(", ", 2);
                LoggerFactory.getLogger(CompetitionMessageListener.class)
                        .debug("Items: " + Arrays.toString(items));
                String requestId = items[0];
                String serializedParams = items[1];

                //DEBT: Very complex conditional logic should refactor
                //Compute
                Object response = null;
                boolean responseOk = true;
                try {
                    response = requestListener.onRequest(serializedParams);
                } catch (Exception e) {
                    LoggerFactory.getLogger(CompetitionMessageListener.class)
                            .info("The user implementation has thrown exception.", e);
                    responseOk = false;
                }


                if (response == null) {
                    LoggerFactory.getLogger(CompetitionMessageListener.class)
                            .info("User implementation has returned \"null\".");
                    responseOk = false;
                }

                if (responseOk) {
                    //Serialize
                    String serializedResponse = response.toString();
                    System.out.println("id = " + requestId + ", req = " + serializedParams + ", resp = " + serializedResponse);

                    //Serialize and respond
                    if (isGoLive) {
                        connection.send(requestId + ", " + serializedResponse);
                        message.acknowledge();
                    }
                    shouldContinue = true;
                }
            } catch (JMSException e) {
                //DEBT: Fix logging (no binding found)
                LoggerFactory.getLogger(CompetitionMessageListener.class).error("Error sending response", e);
            }

            return shouldContinue;
        }
    }

    @FunctionalInterface
    public interface RequestListener {
        Object onRequest(String serializedParam);
    }
}
