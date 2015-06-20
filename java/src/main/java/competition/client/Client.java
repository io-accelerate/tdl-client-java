package competition.client;

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
    public interface ProcessingStrategy {
        void processUsing(DeserializeAndRespondToMessage deserializeAndRespondToMessage) throws JMSException;
    }

    @FunctionalInterface
    public interface ProcessingStrategyBuilder {
        ProcessingStrategy buildOn(CentralQueueConnection connection) throws JMSException;
    }

    private static class RespondToAllRequests implements ProcessingStrategy {
        private CentralQueueConnection connection;

        public RespondToAllRequests(CentralQueueConnection connection) {
            this.connection = connection;
        }

        @Override
        public void processUsing(DeserializeAndRespondToMessage deserializeAndRespondToMessage) throws JMSException {
            Message message = connection.receive();
            while (message != null) {
                Response response = deserializeAndRespondToMessage.onRequest(message);
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
        private CentralQueueConnection connection;

        private PeekAtFirstRequest(CentralQueueConnection connection) {
            this.connection = connection;
        }

        @Override
        public void processUsing(DeserializeAndRespondToMessage deserializeAndRespondToMessage) throws JMSException {
            Message message = connection.receive();
            if (message != null) {
                deserializeAndRespondToMessage.onRequest(message);
            }
        }
    }


    private void performMagic(UserImplementation userImplementation, ProcessingStrategyBuilder processingStrategyBuilder) {
        //Obs: this whole code can be abstracted into something
        try (CentralQueueConnection connection = new CentralQueueConnection(brokerURL, username)){
            DeserializeAndRespondToMessage deserializeAndRespondToMessage
                    = new DeserializeAndRespondToMessage(userImplementation);
            processingStrategyBuilder
                    .buildOn(connection)
                    .processUsing(deserializeAndRespondToMessage);


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

    private static class DeserializeAndRespondToMessage {
        private final UserImplementation userImplementation;

        public DeserializeAndRespondToMessage(UserImplementation userImplementation) {
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
                LoggerFactory.getLogger(DeserializeAndRespondToMessage.class)
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
                    LoggerFactory.getLogger(DeserializeAndRespondToMessage.class)
                            .info("The user implementation has thrown exception.", e);
                    responseOk = false;
                }


                if (result == null) {
                    LoggerFactory.getLogger(DeserializeAndRespondToMessage.class)
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
                LoggerFactory.getLogger(DeserializeAndRespondToMessage.class).error("Error sending response", e);
            }

            return response;
        }
    }

}
