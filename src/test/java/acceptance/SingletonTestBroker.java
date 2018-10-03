package acceptance;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import tdl.client.runner.connector.EventSerializationException;
import tdl.client.runner.connector.QueueEvent;
import tdl.client.runner.connector.QueueSize;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by julianghionoiu on 11/10/2015.
 */
public class SingletonTestBroker {

    private static final String ATTRIBUTE_EVENT_NAME = "name";
    private static final String ATTRIBUTE_EVENT_VERSION = "version";

    private final ObjectMapper mapper;
    private AmazonSQS client;
    private String serviceEndpoint;

    public SingletonTestBroker() {
        logToConsole(" SingletonTestBroker creation [start]");

        Config config = ConfigFactory.load();
        config.checkValid(ConfigFactory.defaultReference());

        serviceEndpoint = config.getString("sqs.serviceEndpoint");
        client = createAWSClient(
                serviceEndpoint,
                config.getString("sqs.signingRegion"),
                config.getString("sqs.accessKey"),
                config.getString("sqs.secretKey")
        );

        mapper = new ObjectMapper();

        logToConsole(" SingletonTestBroker creation [end]");
    }

    public CreateQueueRequest addQueue(String queueName) throws Exception {
        final Map<String, String> attributes = new HashMap<>();
        attributes.put("FifoQueue", "true");
        attributes.put("ContentBasedDeduplication", "true");

        logToConsole(" SingletonTestBroker addQueue");
        return new CreateQueueRequest(queueName)
                .withAttributes(attributes);
    }

    private void logToConsole(String s) {
        if ((System.getenv("DEBUG") != null) && System.getenv("DEBUG").contains("true")) {
            System.out.println(s);
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

    public void send(String queueUrl, Object object) throws EventSerializationException {
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

    private QueueSize getQueueSize(String queueUrl) {
        GetQueueAttributesResult queueAttributes = client
                .getQueueAttributes(queueUrl, Collections.singletonList("All"));
        int available = Integer.parseInt(queueAttributes.getAttributes().get("ApproximateNumberOfMessages"));
        int notVisible = Integer.parseInt(queueAttributes.getAttributes().get("ApproximateNumberOfMessagesNotVisible"));
        int delayed = Integer.parseInt(queueAttributes.getAttributes().get("ApproximateNumberOfMessagesDelayed"));
        return new QueueSize(available, notVisible, delayed);
    }

    public long getSize(CreateQueueRequest queue) {
        String queueUrl = String.format("%s/queue/%s", serviceEndpoint, queue.getQueueName());
        return getQueueSize(queueUrl).getAvailable();
    }

    public void purgeQueue(CreateQueueRequest requestQueue) {
        String queueUrl = String.format("%s/queue/%s", serviceEndpoint, requestQueue.getQueueName());
        client.purgeQueue(new PurgeQueueRequest(queueUrl));
    }

    public String createQueue(CreateQueueRequest responseQueue) {
        return client.createQueue(responseQueue).getQueueUrl();
    }
}
