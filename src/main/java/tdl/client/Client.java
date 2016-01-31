package tdl.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tdl.client.abstractions.Request;
import tdl.client.abstractions.response.Response;
import tdl.client.actions.ClientAction;
import tdl.client.audit.AuditStream;
import tdl.client.audit.Auditable;
import tdl.client.audit.StdoutAuditStream;
import tdl.client.transport.BrokerCommunicationException;
import tdl.client.transport.RemoteBroker;

import java.util.Optional;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class Client {
    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);
    private final String hostname;
    private final int port;
    private final String username;
    private final Audit audit;

    protected Client(String hostname, int port, String username, AuditStream auditStream) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.audit = new Audit(auditStream);
    }

    public static class Builder {
        private String hostname;
        private int port;
        private String username;
        private AuditStream auditStream = new StdoutAuditStream();

        public Builder setHostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setAuditStream(AuditStream auditStream) {
            this.auditStream = auditStream;
            return this;
        }

        public Client create() {
            return new Client(hostname, port, username, auditStream);
        }
    }


    public void goLiveWith(ProcessingRules processingRules) {
        try (RemoteBroker remoteBroker = new RemoteBroker(hostname, port, username)){
            //Design: We use a while loop instead of an ActiveMQ MessageListener to process the messages in order
            Optional<Request> request = remoteBroker.receive();
            while (request.isPresent()) {
                request = applyProcessingRules(request.get(), processingRules, remoteBroker);
            }
        } catch (Exception e) {
            String message = "There was a problem processing messages";
            LOGGER.error(message, e);
            audit.logException(message, e);
        }
    }

    private Optional<Request> applyProcessingRules(
            Request request, ProcessingRules processingRules, RemoteBroker remoteBroker)
            throws BrokerCommunicationException {
        audit.startLine();
        audit.log(request);

        //Obtain response from user
        Response response = processingRules.getResponseFor(request);
        audit.log(response);

        //Obtain action
        ClientAction clientAction = response.getClientAction();

        //Act
        clientAction.afterResponse(remoteBroker, request, response);
        audit.log(clientAction);
        audit.endLine();
        return clientAction.getNextRequest(remoteBroker);
    }

    //~~~~ Utils

    private static class Audit {
        private final AuditStream auditStream;
        private StringBuilder line;

        public Audit(AuditStream auditStream) {
            this.auditStream = auditStream;
            startLine();
        }

        //~~~ Normal output

        public void startLine() {
            line = new StringBuilder();
        }

        public void log(Auditable auditable) {
            String text = auditable.getAuditText();
            if (!text.isEmpty() && line.length() > 0) {
                line.append(", ");
            }
            line.append(text);
        }

        public void endLine() {
            auditStream.println(line.toString());
        }

        //~~~ Exception

        public void logException(String message, Exception e) {
            startLine();
            line.append(message).append(": ").append(e.getMessage());
            endLine();
        }
    }

}
