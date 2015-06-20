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
        run(userImplementation, RespondToAllRequests::new);
    }

    public void trialRunWith(UserImplementation userImplementation) {
        run(userImplementation, PeekAtFirstRequest::new);
    }


    private void run(UserImplementation userImplementation, ProcessingStrategyBuilder processingStrategyBuilder) {
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


    //~~~~ Processing strategies

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
            StringMessage message = connection.receive();
            while (message != null) {
                String response = deserializeAndRespondToMessage.onRequest(message.getContent());
                if ( response == null ) {
                    break;
                }

                connection.send(response);
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
            StringMessage message = connection.receive();
            if (message != null) {
                deserializeAndRespondToMessage.onRequest(message.getContent());
            }
        }
    }

    //~~~~ Transport -> Serialization -> Process

    private static class Request {
        private final String requestId;
        private final String[] params;

        public Request(String requestId, String[] params) {
            this.requestId = requestId;
            this.params = params;
        }

        public String getRequestId() {
            return requestId;
        }

        public String[] getParams() {
            return params;
        }
    }

//    private static class CsvSerializationProvider {
//
//
//        public Request deserialize();
//
//
//    }

    private static class DeserializeAndRespondToMessage {
        private final UserImplementation userImplementation;

        public DeserializeAndRespondToMessage(UserImplementation userImplementation) {
            this.userImplementation = userImplementation;
        }

        public String onRequest(String messageText) {
            Response response = null;

            //Debt: The serialization strategy should be abstracted
            Request request;
            {
                String[] items = messageText.split(", ", 2);
                LoggerFactory.getLogger(DeserializeAndRespondToMessage.class)
                        .debug("Received items: " + Arrays.toString(items));
                String requestId = items[0];
                String serializedParams = items[1];

                String[] params = serializedParams.split(", ");
                request = new Request(requestId, params);
            }


            //DEBT: Very complex conditional logic should refactor
            //Compute
            Object result = null;
            boolean responseOk = true;
            try {
                result = userImplementation.process(request.getParams());
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
                response = new Response(request.getRequestId(), result);
                System.out.println("id = " + request.getRequestId() + ", " +
                        "req = " + Arrays.asList(request.getParams()) + ", " +
                        "resp = " + response.getResult().toString());
            }

            return response.getRequestId() + ", " + response.getResult();
        }
    }

}
