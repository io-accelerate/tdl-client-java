package tdl.client.queue.transport;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.LoggerFactory;
import tdl.client.queue.abstractions.Request;
import tdl.client.queue.abstractions.response.Response;
import tdl.client.queue.serialization.DeserializationException;
import tdl.client.queue.serialization.JsonRpcRequest;
import tdl.client.runner.connector.EventProcessingException;
import tdl.client.runner.connector.EventSerializationException;
import tdl.client.runner.connector.QueueEvent;
import tdl.client.runner.events.ExecuteCommandEvent;

import javax.jms.JMSException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemoteBroker implements AutoCloseable {
    private static final int MAX_NUMBER_OF_MESSAGES = 10;
    private static final int MAX_AWS_WAIT = 1;

    private static final String ATTRIBUTE_EVENT_NAME = "name";
    private static final String ATTRIBUTE_EVENT_VERSION = "version";

    private final ObjectMapper mapper;

    private AmazonSQS client;
    private String serviceEndpoint;

    private ReceiveMessageRequest receiveMessageRequest;
    private DeleteMessageRequest deleteMessageRequest;

    private final CreateQueueRequest messageConsumer;
    private final String messageConsumerQueueUrl;
    private final CreateQueueRequest messageProducer;
    private final String messageProducerQueueUrl;
    private final Gson gson;

    public RemoteBroker(String hostname, int port, String uniqueId, int requestTimeoutMillis) throws JMSException, EventProcessingException {
        logToConsole("     RemoteBroker creation [start]");

        Config config = ConfigFactory.load();
        config.checkValid(ConfigFactory.defaultReference());

        serviceEndpoint = String.format("http://%s:%d", hostname, port);
        client = createAWSClient(
                serviceEndpoint,
                config.getString("sqs.signingRegion"),
                config.getString("sqs.accessKey"),
                config.getString("sqs.secretKey")
        );

        LoggerFactory.getLogger(RemoteBroker.class).debug("Connecting to the remote broker");

        final Map<String, String> attributes = new HashMap<>();
        attributes.put("FifoQueue", "true");
        attributes.put("ContentBasedDeduplication", "true");

        String requestQueue = uniqueId + "-req";
        messageConsumer = new CreateQueueRequest(requestQueue).withAttributes(attributes);
        messageConsumerQueueUrl = client.createQueue(messageConsumer).getQueueUrl();

        receiveMessageRequest = new ReceiveMessageRequest();
        receiveMessageRequest.setMaxNumberOfMessages(MAX_NUMBER_OF_MESSAGES);
        receiveMessageRequest.setQueueUrl(messageConsumerQueueUrl);
        receiveMessageRequest.setWaitTimeSeconds(MAX_AWS_WAIT);
        receiveMessageRequest.setMessageAttributeNames(Arrays.asList(ATTRIBUTE_EVENT_NAME, ATTRIBUTE_EVENT_VERSION));

        String responseQueue = uniqueId + "-resp";
        messageProducer = new CreateQueueRequest(responseQueue).withAttributes(attributes);
        messageProducerQueueUrl = client.createQueue(messageProducer).getQueueUrl();

        mapper = new ObjectMapper();

        gson = new GsonBuilder()
                .serializeNulls()
                .create();

        logToConsole("     RemoteBroker creation [end]");
    }

    public List<Request> receive() throws BrokerCommunicationException {
        logToConsole("     RemoteBroker receive [start]");
        try {
            List<Message> messages;
            List<Request> successfulMessages = new ArrayList<>();
            boolean continueFetching;
            do {
                messages = client.receiveMessage(receiveMessageRequest).getMessages();
                continueFetching = messages.size() > 0;
                if (continueFetching) {
                    logToConsole("     RemoteBroker messages: " + messages);
                    successfulMessages.addAll(processRequests(messages));
                }
            } while (continueFetching);

            logToConsole("     RemoteBroker receive [end]");
            return successfulMessages;
        } catch (Exception ex) {
            logToConsole("     RemoteBroker receive [error]");
            throw new BrokerCommunicationException(ex);
        }
    }

    private List<Request> processRequests(List<Message> messages) throws java.io.IOException, DeserializationException {
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
                logToConsole("     RemoteBroker did not complete reading all messages successfully, not deleting unsuccessful messages");
                throw new DeserializationException("Invalid message format", e);
            }
        }
        return newResult;
    }

    public void deleteMessage(Message message) {
        deleteMessageRequest = new DeleteMessageRequest(messageProducerQueueUrl, message.getReceiptHandle());
        client.deleteMessage(deleteMessageRequest);
    }

    public void respondTo(Request request, Response response) throws BrokerCommunicationException {
        logToConsole("     RemoteBroker respondTo [start]");
        logToConsole("     response: " + response);
        try {
            send(messageProducerQueueUrl, new ExecuteCommandEvent(response.toString()));

            logToConsole("     RemoteBroker respondTo [end]");
        } catch (Exception e) {
            logToConsole("     RemoteBroker respondTo [error]");
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

    @Override
    public void close() throws Exception {
        logToConsole("     RemoteBroker close [start]");
        LoggerFactory.getLogger(RemoteBroker.class).debug("Stopping the connection to the broker");
//        client.shutdown();
        logToConsole("     RemoteBroker close [end]");
    }

    private static AmazonSQS createAWSClient(String serviceEndpoint, String signingRegion, String accessKey, String secretKey) {
        AwsClientBuilder.EndpointConfiguration endpointConfiguration =
                new AwsClientBuilder.EndpointConfiguration(serviceEndpoint, signingRegion);
        return AmazonSQSClientBuilder.standard()
                .withEndpointConfiguration(endpointConfiguration)
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .build();
    }

    private void logToConsole(String s) {
        if ((System.getenv("DEBUG") != null) && System.getenv("DEBUG").contains("true")) {
            System.out.println(s);
        }
    }
}
