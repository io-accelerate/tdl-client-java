package tdl.client;

import tdl.client.abstractions.ProcessingRules;
import tdl.client.abstractions.Request;
import tdl.client.abstractions.Response;
import tdl.client.abstractions.ProcessingRule;
import tdl.client.actions.ClientAction;
import tdl.client.actions.StopAction;
import tdl.client.audit.AuditStream;
import tdl.client.audit.Auditable;
import tdl.client.audit.StdoutAuditStream;
import tdl.client.transport.RemoteBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
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

    public Client(String hostname, int port, String username) {
        this(hostname, port, username, new StdoutAuditStream());
    }

    protected Client(String hostname, int port, String username, AuditStream auditStream) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.audit = new Audit(auditStream);
    }

    public void goLiveWith(ProcessingRules processingRules) {
        try (RemoteBroker remoteBroker = new RemoteBroker(hostname, port, username)){
            //Design: Use a while loop instead of an ActiveMQ MessageListener to process the messages in order
            Optional<Request> request = remoteBroker.receive();
            while (request.isPresent()) {
                request = applyProcessingRules(request.get(), processingRules, remoteBroker);
            }
        } catch (Exception e) {
            LOGGER.error("There was a problem processing messages", e);
        }
    }

    private Optional<Request> applyProcessingRules(
            Request request, ProcessingRules processingRules, RemoteBroker remoteBroker)
            throws JMSException {
        audit.startLine();
        audit.log(request);

        //Obtain response from user
        ProcessingRule processingRule = processingRules.getRuleFor(request);
        Optional<Response> optionalResponse = getResponseFor(request, processingRule);
        audit.log(optionalResponse.orElse(Response.EMPTY));

        //Obtain action
        ClientAction clientAction = optionalResponse
                .map(response -> processingRule.getClientAction())
                .orElse(new StopAction());

        //Act
        clientAction.afterResponse(remoteBroker, request, optionalResponse.orElse(Response.EMPTY));
        audit.log(clientAction);
        audit.endLine();
        return clientAction.getNextRequest(remoteBroker);
    }

    private Optional<Response> getResponseFor(Request request, ProcessingRule processingRule) {
        Optional<Response> response;
        try {
            Object result = processingRule.getUserImplementation().process(request.getParams());
            response = Optional.of(new Response(request.getId(), result));
        } catch (Exception e) {
            response = Optional.empty();
            LoggerFactory.getLogger(Client.class)
                    .warn("The user implementation has thrown exception.", e);
        }
        return response;
    }

    //~~~~ Utils

    private static class Audit {
        private final AuditStream auditStream;
        private StringBuilder line;

        public Audit(AuditStream auditStream) {
            this.auditStream = auditStream;
            startLine();
        }

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
    }



}
