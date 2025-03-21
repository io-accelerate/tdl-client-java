package io.accelerate.client.queue;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.accelerate.client.audit.PresentationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.accelerate.client.audit.AuditStream;
import io.accelerate.client.queue.abstractions.Request;
import io.accelerate.client.queue.abstractions.UserImplementation;
import io.accelerate.client.queue.abstractions.response.FatalErrorResponse;
import io.accelerate.client.queue.abstractions.response.Response;
import io.accelerate.client.queue.transport.BrokerCommunicationException;
import io.accelerate.client.queue.transport.RemoteBroker;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QueueBasedImplementationRunner implements ImplementationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueueBasedImplementationRunner.class);
    private final Audit audit;
    private final ProcessingRules deployProcessingRules;
    private final ImplementationRunnerConfig config;
    private final ObjectMapper objectMapper;
    
    private QueueBasedImplementationRunner(ImplementationRunnerConfig config, ProcessingRules deployProcessingRules, List<Module> additionalJacksonModules) {
        this.config = config;
        this.deployProcessingRules = deployProcessingRules;
        this.objectMapper = JsonMapper.builder()
                .configure(MapperFeature.ALLOW_COERCION_OF_SCALARS, false)
                .build();
        this.objectMapper.registerModules(additionalJacksonModules);
        this.audit = new Audit(config.getAuditStream(), objectMapper);
    }

    public static class Builder {
        private final ProcessingRules deployProcessingRules;
        private ImplementationRunnerConfig config;
        private List<Module> additionalJacksonModules;

        public Builder() {
            deployProcessingRules = createDeployProcessingRules();
            additionalJacksonModules = new ArrayList<>();
        }

        public Builder setConfig(ImplementationRunnerConfig config) {
            this.config = config;
            return this;
        }

        @SuppressWarnings({"unused", "UnusedReturnValue"})
        public Builder withSolutionFor(String methodName, UserImplementation userImplementation) {
            deployProcessingRules
                    .on(methodName)
                    .call(userImplementation)
                    .build();
            return this;
        }
        
        @SuppressWarnings("unused")
        public Builder withJacksonModule(Module module) {
            additionalJacksonModules.add(module);
            return this;
        }

        public QueueBasedImplementationRunner create() {
            return new QueueBasedImplementationRunner(config, deployProcessingRules, additionalJacksonModules);
        }

        private static ProcessingRules createDeployProcessingRules() {
            ProcessingRules deployProcessingRules = new ProcessingRules();

            // Debt - we only need this to consume message from the server
            deployProcessingRules
                    .on("display_description")
                    .call(params -> "OK")
                    .build();

            return deployProcessingRules;
        }
    }

    public void run() {
        audit.logLine("Starting client");
        try (RemoteBroker remoteBroker = new RemoteBroker(
                config.getHostname(),
                config.getPort(),
                config.getRequestTimeoutMillis(),
                config.getRequestQueueName(),
                config.getResponseQueueName(),
                objectMapper)) {
            //Design: We use a while loop instead of an ActiveMQ MessageListener to process the messages in order
            audit.logLine("Waiting for requests");
            Optional<Request> request = remoteBroker.receive();
            while (request.isPresent()) {
                request = applyProcessingRules(request.get(), deployProcessingRules, remoteBroker);
            }
        } catch (Exception e) {
            String message = "There was a problem processing messages";
            LOGGER.error(message, e);
            audit.logException(message, e);
        }
        audit.logLine("Stopping client");
    }

    public int getRequestTimeoutMillis() {
        return config.getRequestTimeoutMillis();
    }

    private Optional<Request> applyProcessingRules(
            Request request, ProcessingRules processingRules, RemoteBroker remoteBroker)
            throws BrokerCommunicationException {
        audit.startLine();
        audit.logRequest(request);

        //Obtain response from user
        Response response = processingRules.getResponseFor(request);
        audit.logResponse(response);

        //Act
        if (response instanceof FatalErrorResponse) {
            audit.endLine();
            return Optional.empty();
        }

        remoteBroker.respondTo(request, with(response));
        audit.endLine();

        return remoteBroker.receive();
    }

    <T> T with(T obj) {
        return obj;
    }

    //~~~~ Utils

    private static class Audit {
        private final AuditStream auditStream;
        private final List<String> messages = new ArrayList<>();
        private final PresentationUtils presentationUtils;

        Audit(AuditStream auditStream, ObjectMapper objectMapper) {
            this.auditStream = auditStream;
            presentationUtils = new PresentationUtils(objectMapper);
        }

        //~~~ Normal output

        void startLine() {
            messages.clear();
        }

        void logRequest(Request request) {
            String requestLogLine = String.format("id = %s, req = %s(%s)",
                    request.getId(), request.getMethodName(), presentationUtils.toDisplayableRequest(request.getParams()));
            messages.add(requestLogLine);
        }

        void logResponse(Response response) {
            String responseLogLine ;
            if (response.isError()) {
                responseLogLine = String.format("%s = \"%s\", %s",  response.id(), response.result(), "(NOT PUBLISHED)" );
            } else {
                responseLogLine = String.format("resp = %s", presentationUtils.toDisplayableResponse(response.result()));
            }
            messages.add(responseLogLine);
        }

        void endLine() {
            auditStream.println(String.join(", ", messages));
        }

        //~~~ Exception

        void logException(String message, Exception e) {
            startLine();
            messages.add(message + ": " + e.getMessage());
            endLine();
        }

        void logLine(String text) {
            startLine();
            messages.add(text);
            endLine();
        }
    }

}
