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

    public Client(String brokerURL, String username) {
        this.brokerURL = brokerURL;
        this.username = username;
    }

    public void goLiveWith(UserImplementation userImplementation) {
        performMagic(userImplementation, connection -> new RespondToAllRequests(connection));

    }

    public void trialRunWith(UserImplementation userImplementation) {
        performMagic(userImplementation, connection -> new PeekAtFirstRequest(connection));
    }

    @FunctionalInterface
    private interface ProcessingStrategy {
        void processUsing(MessageProcessor messageProcessor) throws JMSException;
    }

    @FunctionalInterface
    private interface ProcessingStrategyBuilder {
        ProcessingStrategy buildOn(CompetitionServerConnection connection) throws JMSException;
    }

    private static class RespondToAllRequests implements ProcessingStrategy {
        private CompetitionServerConnection connection;

        public RespondToAllRequests(CompetitionServerConnection connection) {
            this.connection = connection;
        }

        @Override
        public void processUsing(MessageProcessor messageProcessor) throws JMSException {
            Message message = connection.receive();
            while (message != null) {
                Response response = messageProcessor.onRequest(message);
                if ( response == null ) {
                    break;
                }

                connection.send(response.getRequestId() + ", " + response.getResult());
                message.acknowledge();

                //Obs: This method could exit faster if we put a special close message to the queue
                message = connection.receive();
            }
        }
    }

    private static class PeekAtFirstRequest implements ProcessingStrategy {
        private CompetitionServerConnection connection;

        private PeekAtFirstRequest(CompetitionServerConnection connection) {
            this.connection = connection;
        }

        @Override
        public void processUsing(MessageProcessor messageProcessor) throws JMSException {
            Message message = connection.receive();
            if (message != null) {
                messageProcessor.onRequest(message);
            }
        }
    }


    private void performMagic(UserImplementation userImplementation, ProcessingStrategyBuilder processingStrategyBuilder) {
        //Obs: this whole code can be abstracted into something
        try (CompetitionServerConnection connection = new CompetitionServerConnection(brokerURL, username)){
            MessageProcessor messageProcessor = new MessageProcessor(userImplementation);
            processingStrategyBuilder
                    .buildOn(connection)
                    .processUsing(messageProcessor);


            LoggerFactory.getLogger(Client.class).info("Stopping client.");
        } catch (Exception e) {
            LOGGER.error("Problem communicating with the broker", e);
        }
    }

    private static class Response {
        private String requestId;
        private Object result;

        public Response(String requestId, Object result) {
            this.requestId = requestId;
            this.result = result;
        }

        public String getRequestId() {
            return requestId;
        }

        public Object getResult() {
            return result;
        }
    }

    private static class CompetitionServerConnection implements AutoCloseable {
        private static final long REQUEST_TIMEOUT = 1000L;

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

        public Message receive() throws JMSException {

            //Obs: We should have no timeout
            return messageConsumer.receive(REQUEST_TIMEOUT);
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


    private static class MessageProcessor {
        private final UserImplementation userImplementation;

        public MessageProcessor(UserImplementation userImplementation) {
            this.userImplementation = userImplementation;
        }

        public Response onRequest(Message message) {
            Response response = null;
            try {
                //Debt: The serialization strategy should be abstracted
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
                LoggerFactory.getLogger(MessageProcessor.class)
                        .debug("Items: " + Arrays.toString(items));
                String requestId = items[0];
                String serializedParams = items[1];

                //DEBT: Very complex conditional logic should refactor
                //Compute
                Object result = null;
                boolean responseOk = true;
                try {
                    result = userImplementation.process(serializedParams);
                } catch (Exception e) {
                    LoggerFactory.getLogger(MessageProcessor.class)
                            .info("The user implementation has thrown exception.", e);
                    responseOk = false;
                }


                if (result == null) {
                    LoggerFactory.getLogger(MessageProcessor.class)
                            .info("User implementation has returned \"null\".");
                    responseOk = false;
                }

                if (responseOk) {
                    //Serialize
                    String serializedResponse = result.toString();
                    response = new Response(requestId, result);
                    System.out.println("id = " + requestId + ", req = " + serializedParams + ", resp = " + serializedResponse);
                }
            } catch (JMSException e) {
                LoggerFactory.getLogger(MessageProcessor.class).error("Error sending response", e);
            }

            return response;
        }
    }

    //Obs: serializedParam can become Request and the returned object could be called Response
    @FunctionalInterface
    public interface UserImplementation {
        Object process(String serializedParam);
    }
}
