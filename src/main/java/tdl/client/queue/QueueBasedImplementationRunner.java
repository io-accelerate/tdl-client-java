package tdl.client.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tdl.client.audit.AuditStream;
import tdl.client.audit.Auditable;
import tdl.client.queue.abstractions.Request;
import tdl.client.queue.abstractions.UserImplementation;
import tdl.client.queue.abstractions.response.Response;
import tdl.client.queue.actions.ClientAction;
import tdl.client.queue.transport.RemoteBroker;

import java.util.ArrayList;
import java.util.List;

import static tdl.client.queue.actions.ClientActions.publish;

public class QueueBasedImplementationRunner implements ImplementationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueueBasedImplementationRunner.class);
    private final Audit audit;
    private final ProcessingRules deployProcessingRules;
    private final ImplementationRunnerConfig config;
    private List<Response> responses;
    private List<Request> requests;
    private RemoteBroker remoteBroker;

    private QueueBasedImplementationRunner(ImplementationRunnerConfig config, ProcessingRules deployProcessingRules) {
        logToConsole("        QueueBasedImplementationRunner creation [start]");
        this.config = config;
        this.deployProcessingRules = deployProcessingRules;
        audit = new Audit(config.getAuditStream());

        responses = new ArrayList<>();
        requests = new ArrayList<>();

        logToConsole("        QueueBasedImplementationRunner creation [end]");
    }

    public List<String> getReceivedMessages() {
        List<String> results = new ArrayList<>();

        for (Response response : responses) {

            String result;
            if ((response.getResult() == null) ||
                    isNumeric(response.getResult())) {
                result = String.format(
                        "{\"result\":%s,\"error\":null,\"id\":\"%s\"}",
                        response.getResult(), response.getId());
            } else {
                result = String.format(
                        "{\"result\":\"%s\",\"error\":null,\"id\":\"%s\"}",
                        response.getResult(), response.getId());
            }

            results.add(result);
        }
        responses.clear();

        return results;
    }

    public static class Builder {
        private final ProcessingRules deployProcessingRules;
        private ImplementationRunnerConfig config;

        public Builder() {
            deployProcessingRules = createDeployProcessingRules();
        }

        public Builder setConfig(ImplementationRunnerConfig config) {
            this.config = config;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder withSolutionFor(String methodName, UserImplementation userImplementation) {
            deployProcessingRules
                    .on(methodName)
                    .call(userImplementation)
                    .then(publish());
            return this;
        }

        public Builder withSolutionFor(String methodName, UserImplementation userImplementation, ClientAction action) {
            deployProcessingRules
                    .on(methodName)
                    .call(userImplementation)
                    .then(action);
            return this;
        }

        public QueueBasedImplementationRunner create() {
            logToConsole("        QueueBasedImplementationRunner.Builder create");
            logToConsole("        QueueBasedImplementationRunner.Builder deployProcessingRules: " + deployProcessingRules);
            return new QueueBasedImplementationRunner(config, deployProcessingRules);
        }

        private static ProcessingRules createDeployProcessingRules() {
            logToConsole("        QueueBasedImplementationRunner.Builder createDeployProcessingRules [start]");
            ProcessingRules deployProcessingRules = new ProcessingRules();

            // Debt - we only need this to consume message from the server
            deployProcessingRules
                    .on("display_description")
                    .call(params -> "OK")
                    .then(publish());

            logToConsole("        QueueBasedImplementationRunner.Builder createDeployProcessingRules [end]");
            return deployProcessingRules;
        }
    }

    public void run() {
        logToConsole("        QueueBasedImplementationRunner run [start]");
        audit.logLine("Starting client");
        requests.clear();
        try {
            requests.clear();
            remoteBroker = new RemoteBroker(config.getHostname(), config.getPort(), config.getUniqueId(), config.getRequestTimeoutMillis());

            requests.addAll(remoteBroker.receive());

            audit.logLine("Waiting for requests");
            logToConsole("        QueueBasedImplementationRunner requests: " + requests.size());

            responses.clear();
            for (int requestIndex = requests.size() - 1; requestIndex >= 0; requestIndex--) {
                Request request = requests.get(requestIndex);
                logToConsole("        QueueBasedImplementationRunner applyProcessingRules [start]");
                audit.startLine();
                audit.log(request);
                requests.add(request);

                //Obtain response from user
                Response response = deployProcessingRules.getResponseFor(request);
                audit.log(response);
                responses.add(response);

                //Obtain action
                ClientAction clientAction = response.getClientAction();

                //Act
                clientAction.afterResponse(remoteBroker, request, response);
                logToConsole("        QueueBasedImplementationRunner applyProcessingRules clientAction: " + clientAction);
                audit.log(clientAction);
                audit.endLine();
                logToConsole("        QueueBasedImplementationRunner applyProcessingRules [end]");

                remoteBroker.deleteMessage(request.getOriginalMessage());
                logToConsole("        QueueBasedImplementationRunner deleting read message");

                String simpleClientActionName = clientAction.getClass().getSimpleName();
                if ("PublishAction".equals(simpleClientActionName)) {
                } else if ("PublishAndStopAction".equals(simpleClientActionName)) {
                    break;
                } else if ("StopAction".equals(simpleClientActionName)) {
                    break;
                }
            }
        } catch (Exception e) {
            logToConsole("        QueueBasedImplementationRunner run [error]");
            String message = "There was a problem processing messages";
            LOGGER.error(message, e);
            audit.logException(message, e);
        }
        audit.logLine("Stopping client");
        logToConsole("        QueueBasedImplementationRunner run [end]");
    }

    public int getRequestTimeoutMillis() {
        return config.getRequestTimeoutMillis();
    }


    //~~~~ Utils
    private boolean isNumeric(Object result) {
        return result.toString().chars().allMatch(Character::isDigit);
    }

    private static class Audit {
        private final AuditStream auditStream;
        private StringBuilder line;

        Audit(AuditStream auditStream) {
            this.auditStream = auditStream;
            startLine();
        }

        //~~~ Normal output

        void startLine() {
            line = new StringBuilder();
        }

        void log(Auditable auditable) {
            String text = auditable.getAuditText();
            if (!text.isEmpty() && line.length() > 0) {
                line.append(", ");
            }
            line.append(text);
        }

        void endLine() {
            auditStream.println(line.toString());
        }

        //~~~ Exception

        void logException(String message, Exception e) {
            startLine();
            line.append(message).append(": ").append(e.getMessage());
            endLine();
        }

        void logLine(String text) {
            startLine();
            this.line.append(text);
            endLine();
        }
    }

    private static void logToConsole(String s) {
        if ((System.getenv("DEBUG") != null) && System.getenv("DEBUG").contains("true")) {
            System.out.println(s);
        }
    }
}
