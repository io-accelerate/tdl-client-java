package competition.client;

import competition.client.abstractions.UserImplementation;
import competition.client.transport.RemoteBroker;
import competition.client.transport.StringMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import java.io.PrintStream;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class Client {
    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);
    private final String hostname;
    private final int port;
    private final String username;
    private final PrintStream auditStream;

    public Client(String hostname, int port, String username) {
        this(hostname, port, username, System.out);
    }

    protected Client(String hostname, int port, String username, PrintStream auditStream) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.auditStream = auditStream;
    }

    public void goLiveWith(UserImplementation userImplementation) {
        run(new RespondToAllRequests(DeserializeAndRespondToMessage.using(userImplementation)));
    }

    public void trialRunWith(UserImplementation userImplementation) {
        run(new PeekAtFirstRequest(DeserializeAndRespondToMessage.using(userImplementation)));
    }

    private void run(HandlingStrategy strategy) {
        try (RemoteBroker remoteBroker = new RemoteBroker(hostname, port, username)){
            strategy.processNextMessageFrom(remoteBroker);

            LoggerFactory.getLogger(Client.class).info("Stopping client.");
        } catch (Exception e) {
            LOGGER.error("Problem communicating with the broker", e);
        }
    }

    //~~~~ Abstractions

    @FunctionalInterface
    public interface HandlingStrategy {
        void processNextMessageFrom(RemoteBroker remoteBroker) throws JMSException;
    }

    //~~~~ Queue handling policies

    private static class RespondToAllRequests implements HandlingStrategy {
        private MessageHandler messageHandler;

        public RespondToAllRequests(MessageHandler messageHandler) {
            this.messageHandler = messageHandler;
        }

        @Override
        public void processNextMessageFrom(RemoteBroker remoteBroker) throws JMSException {
            StringMessage message = remoteBroker.receive();
            while (message.isValid()) {
                String response = messageHandler.respondTo(message.getContent());
                if ( response == null ) {
                    break;
                }

                remoteBroker.send(response);
                message.acknowledge();

                //Obs: This method could exit faster if we put a special close message to the queue
                message = remoteBroker.receive();
            }
        }
    }

    private static class PeekAtFirstRequest implements HandlingStrategy {
        private MessageHandler messageHandler;

        private PeekAtFirstRequest(MessageHandler messageHandler) {
            this.messageHandler = messageHandler;
        }

        @Override
        public void processNextMessageFrom(RemoteBroker remoteBroker) throws JMSException {
            StringMessage message = remoteBroker.receive();
            if (message.isValid()) {
                messageHandler.respondTo(message.getContent());
            }
        }
    }
}
