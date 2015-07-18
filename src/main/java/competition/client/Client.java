package competition.client;

import competition.client.abstractions.UserImplementation;
import competition.client.transport.CentralQueueConnection;
import competition.client.transport.StringMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class Client {
    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);
    private final String brokerURL;
    private final String username;

    public Client(String hostname, int port, String username) {
        this.brokerURL = String.format("tcp://%s:%s", hostname, port);
        this.username = username;
    }

    public void goLiveWith(UserImplementation userImplementation) {
        run(RespondToAllRequests::new, userImplementation);
    }

    public void trialRunWith(UserImplementation userImplementation) {
        run(PeekAtFirstRequest::new, userImplementation);
    }

    private void run(ProcessingStrategyBuilder processingStrategyBuilder, UserImplementation userImplementation) {
        try (CentralQueueConnection connection = new CentralQueueConnection(brokerURL, username)){
            DeserializeAndProcessMessage deserializeAndProcessMessage
                    = new DeserializeAndProcessMessage(userImplementation);

            ProcessingStrategy strategy = processingStrategyBuilder.buildOn(connection);
            strategy.processUsing(deserializeAndProcessMessage);

            LoggerFactory.getLogger(Client.class).info("Stopping client.");
        } catch (Exception e) {
            LOGGER.error("Problem communicating with the broker", e);
        }
    }

    //~~~~ Abstractions

    @FunctionalInterface
    public interface ProcessingStrategy {
        void processUsing(DeserializeAndProcessMessage deserializeAndProcessMessage) throws JMSException;
    }

    @FunctionalInterface
    public interface ProcessingStrategyBuilder {
        ProcessingStrategy buildOn(CentralQueueConnection connection) throws JMSException;
    }

    //~~~~ Processing strategies

    private static class RespondToAllRequests implements ProcessingStrategy {
        private CentralQueueConnection connection;

        public RespondToAllRequests(CentralQueueConnection connection) {
            this.connection = connection;
        }

        @Override
        public void processUsing(DeserializeAndProcessMessage deserializeAndProcessMessage) throws JMSException {
            StringMessage message = connection.receive();
            while (message.isValid()) {
                String response = deserializeAndProcessMessage.onRequest(message.getContent());
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
        public void processUsing(DeserializeAndProcessMessage deserializeAndProcessMessage) throws JMSException {
            StringMessage message = connection.receive();
            if (message.isValid()) {
                deserializeAndProcessMessage.onRequest(message.getContent());
            }
        }
    }
}
