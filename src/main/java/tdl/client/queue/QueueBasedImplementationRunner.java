package tdl.client.queue;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tdl.client.audit.AuditStream;
import tdl.client.audit.Auditable;
import tdl.client.queue.abstractions.Request;
import tdl.client.queue.abstractions.UserImplementation;
import tdl.client.queue.abstractions.response.Response;
import tdl.client.queue.actions.ClientAction;
import tdl.client.queue.serialization.DeserializationException;
import tdl.client.queue.serialization.JsonRpcRequest;
import tdl.client.queue.transport.BrokerCommunicationException;
import tdl.client.runner.connector.EventSerializationException;
import tdl.client.runner.connector.QueueEvent;
import tdl.client.runner.connector.QueueSize;
import tdl.client.runner.events.ExecuteCommandEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tdl.client.queue.actions.ClientActions.publish;

public class QueueBasedImplementationRunner implements ImplementationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueueBasedImplementationRunner.class);
    private final Audit audit;
    private final ProcessingRules deployProcessingRules;
    private final ImplementationRunnerConfig config;

    //TODO: scaffolding till we get our request queue-count right
    private long consumedMessages;
    private long receivedMessages;

    private List<Response> responses;
    private List<Request> requests;

    private static final int MAX_NUMBER_OF_MESSAGES = 10;
    private static final int MAX_AWS_WAIT = 1;

    private static final String ATTRIBUTE_EVENT_NAME = "name";
    private static final String ATTRIBUTE_EVENT_VERSION = "version";

    private final ObjectMapper mapper;

    private AmazonSQS client;
    private String serviceEndpoint;

    private ReceiveMessageRequest receiveRequestQueueMessage;

    private CreateQueueRequest messageRequestQueue;
    private String messageRequestQueueUrl;
    private CreateQueueRequest messageResponse;
    private String messageResponseQueueUrl;
    private final Gson gson;

    private QueueBasedImplementationRunner(ImplementationRunnerConfig config, ProcessingRules deployProcessingRules) {
        logToConsole("        QueueBasedImplementationRunner creation [start]");
        this.config = config;
        this.deployProcessingRules = deployProcessingRules;
        audit = new Audit(config.getAuditStream());

        responses = new ArrayList<>();
        requests = new ArrayList<>();

        logToConsole("     QueueBasedImplementationRunner creation [start]");

        String hostname = config.getHostname();
        int port = config.getPort();
        String uniqueId = config.getUniqueId();

        Config customConfig = ConfigFactory.load();
        customConfig.checkValid(ConfigFactory.defaultReference());
        serviceEndpoint = String.format("http://%s:%d", hostname, port);
        client = createAWSClient(
                serviceEndpoint,
                customConfig.getString("sqs.signingRegion"),
                customConfig.getString("sqs.accessKey"),
                customConfig.getString("sqs.secretKey")
        );

        try {
            messageRequestQueue = createQueueRequest(uniqueId + "-req");
            messageRequestQueueUrl = createQueue(messageRequestQueue);
            messageResponse = createQueueRequest(uniqueId + "-resp");
            messageResponseQueueUrl = createQueue(messageResponse);
        } catch (Exception ex) {
            logToConsole("        QueueBasedImplementationRunner [error]");
            String message = "There was a problem creating the queue runner";
            LOGGER.error(message, ex);
            audit.logException(message, ex);
        }

        receiveRequestQueueMessage = new ReceiveMessageRequest();
        receiveRequestQueueMessage.setMaxNumberOfMessages(MAX_NUMBER_OF_MESSAGES);
        receiveRequestQueueMessage.setQueueUrl(messageRequestQueueUrl);
        receiveRequestQueueMessage.setWaitTimeSeconds(MAX_AWS_WAIT);
        receiveRequestQueueMessage.setMessageAttributeNames(Arrays.asList(ATTRIBUTE_EVENT_NAME, ATTRIBUTE_EVENT_VERSION));

        mapper = new ObjectMapper();

        gson = new GsonBuilder()
                .serializeNulls()
                .create();

        logToConsole("        QueueBasedImplementationRunner creation [end]");
    }

    //TODO: scaffolding till we get our request queue-count right
    public long getConsumedMessagesCount() {
        return consumedMessages;
    }

    //TODO: scaffolding till we get our request queue-count right
    public long getReceivedMessagesCount() {
        return receivedMessages;
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

    public CreateQueueRequest getRequestQueue() {
        return messageRequestQueue;
    }

    public void purgeRequestQueue() {
        purgeQueue(messageRequestQueue);
    }

    public CreateQueueRequest getResponseQueue() {
        return messageResponse;
    }

    public void purgeResponseQueue() {
        purgeQueue(messageResponse);
    }

    public void sendRequest(ExecuteCommandEvent executeCommandEvent) throws EventSerializationException {
        send(messageRequestQueueUrl, executeCommandEvent);
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
            requests.addAll(receive());

            audit.logLine("Waiting for requests");
            logToConsole("        QueueBasedImplementationRunner requests: " + requests.size());
            //TODO: scaffolding till we get our request queue-count right
            receivedMessages = requests.size();
            consumedMessages = 0;

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
                clientAction.afterResponse(this, request, response);
                logToConsole("        QueueBasedImplementationRunner applyProcessingRules clientAction: " + clientAction);
                audit.log(clientAction);
                audit.endLine();
                logToConsole("        QueueBasedImplementationRunner applyProcessingRules [end]");

                deleteMessage(request.getOriginalMessage());
                logToConsole("        QueueBasedImplementationRunner deleting read message");

                String simpleClientActionName = clientAction.getClass().getSimpleName();
                if ("PublishAction".equals(simpleClientActionName)) {
                    consumedMessages++;
                } else if ("PublishAndStopAction".equals(simpleClientActionName)) {
                    consumedMessages++;
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

    private List<Request> receive() throws BrokerCommunicationException {
        logToConsole("     QueueBasedImplementationRunner receive [start]");
        try {
            List<Message> messages;
            List<Request> successfulMessages = new ArrayList<>();
            boolean continueFetching;
            do {
                messages = client.receiveMessage(receiveRequestQueueMessage).getMessages();
                continueFetching = messages.size() > 0;
                if (continueFetching) {
                    logToConsole("     QueueBasedImplementationRunner messages: " + messages);
                    List<Request> newResult = new ArrayList<>();
                    for (Message message : messages) {
                        logToConsole("message: " + message);
                        logToConsole("payload: " + message.getBody());

                        try {
                            JsonNode jsonNode = mapper.readValue(message.getBody(), JsonNode.class);
                            String payload = jsonNode.get("payload").asText();
                            JsonRpcRequest jsonRpcRequest = gson.fromJson(payload, JsonRpcRequest.class);
                            newResult.add(new Request(message, jsonRpcRequest));
                        } catch (JsonSyntaxException e) {
                            logToConsole("     QueueBasedImplementationRunner did not complete reading all messages successfully, not deleting unsuccessful messages");
                            throw new DeserializationException("Invalid message format", e);
                        }
                    }
                    successfulMessages.addAll(newResult);
                }
            } while (continueFetching);

            logToConsole("     QueueBasedImplementationRunner receive [end]");
            return successfulMessages;
        } catch (Exception ex) {
            logToConsole("     QueueBasedImplementationRunner receive [error]");
            throw new BrokerCommunicationException(ex);
        }
    }

    private void deleteMessage(Message message) {
        DeleteMessageRequest deleteMessageRequest = new DeleteMessageRequest(messageResponseQueueUrl, message.getReceiptHandle());
        client.deleteMessage(deleteMessageRequest);
    }

    public void respondTo(Request request, Response response) throws BrokerCommunicationException {
        logToConsole("     QueueBasedImplementationRunner respondTo [start]");
        logToConsole("     response: " + response);
        try {
            send(messageResponseQueueUrl, new ExecuteCommandEvent(response.toString()));

            logToConsole("     QueueBasedImplementationRunner respondTo [end]");
        } catch (Exception e) {
            logToConsole("     QueueBasedImplementationRunner respondTo [error]");
            throw new BrokerCommunicationException(e);
        }
    }

    private void send(String queueUrl, Object object) throws EventSerializationException {
        QueueEvent annotation = object.getClass().getAnnotation(QueueEvent.class);
        if (annotation == null) {
            throw new EventSerializationException(object.getClass() + " not a QueueEvent");
        }
        String eventName = annotation.name();
        String eventVersion = annotation.version();

        try {
            SendMessageRequest sendMessageRequest = new SendMessageRequest();
            sendMessageRequest.setQueueUrl(queueUrl);
            sendMessageRequest.setMessageBody(mapper.writeValueAsString(object));
            sendMessageRequest.addMessageAttributesEntry(ATTRIBUTE_EVENT_NAME,
                    new MessageAttributeValue().withDataType("String").withStringValue(eventName));
            sendMessageRequest.addMessageAttributesEntry(ATTRIBUTE_EVENT_VERSION,
                    new MessageAttributeValue().withDataType("String").withStringValue(eventVersion));
            client.sendMessage(sendMessageRequest);
        } catch (Exception e) {
            throw new EventSerializationException("Failed to serialize event of type " + object.getClass(), e);
        }
    }

    private static AmazonSQS createAWSClient(String serviceEndpoint, String signingRegion, String accessKey, String secretKey) {
        AwsClientBuilder.EndpointConfiguration endpointConfiguration =
                new AwsClientBuilder.EndpointConfiguration(serviceEndpoint, signingRegion);
        return AmazonSQSClientBuilder.standard()
                .withEndpointConfiguration(endpointConfiguration)
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .build();
    }

    private CreateQueueRequest createQueueRequest(String queueName) {
        final Map<String, String> attributes = new HashMap<>();
        attributes.put("FifoQueue", "true");
        attributes.put("ContentBasedDeduplication", "true");

        logToConsole(" QueueBasedImplementationRunner createQueueRequest: " + queueName);
        return new CreateQueueRequest(queueName)
                .withAttributes(attributes);
    }

    private String createQueue(CreateQueueRequest queue) {
        return client.createQueue(queue).getQueueUrl();
    }

    private QueueSize getQueueSize(String queueUrl) {
        GetQueueAttributesResult queueAttributes = client
                .getQueueAttributes(queueUrl, Collections.singletonList("All"));
        int available = Integer.parseInt(queueAttributes.getAttributes().get("ApproximateNumberOfMessages"));
        int notVisible = Integer.parseInt(queueAttributes.getAttributes().get("ApproximateNumberOfMessagesNotVisible"));
        int delayed = Integer.parseInt(queueAttributes.getAttributes().get("ApproximateNumberOfMessagesDelayed"));
        return new QueueSize(available, notVisible, delayed);
    }

    private long getSize(CreateQueueRequest queue) {
        String queueUrl = String.format("%s/queue/%s", serviceEndpoint, queue.getQueueName());
        return getQueueSize(queueUrl).getAvailable();
    }

    public long getRequestQueueSize() {
        return getSize(messageRequestQueue);
    }

    public long getResponseQueueSize() {
        return getSize(messageRequestQueue);
    }

    private void purgeQueue(CreateQueueRequest requestQueue) {
        String queueUrl = String.format("%s/queue/%s", serviceEndpoint, requestQueue.getQueueName());
        client.purgeQueue(new PurgeQueueRequest(queueUrl));
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
        if (new File("DEBUG").exists()) {
            System.out.println(s);
        }
    }
}
