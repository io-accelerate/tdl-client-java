package tdl.client.runner.connector;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class SqsEventQueue {
    private static final Logger log = LoggerFactory.getLogger(SqsEventQueue.class);
    private static final String ATTRIBUTE_EVENT_NAME = "name";
    private static final String ATTRIBUTE_EVENT_VERSION = "version";
    private final AmazonSQS client;
    private final String queueUrl;
    private final ObjectMapper mapper;
    private String queueName;

    public SqsEventQueue(AmazonSQS client, String queueUrl) {
        this.client = client;
        this.queueUrl = queueUrl;
        this.mapper = new ObjectMapper();
    }

    public SqsEventQueue(AmazonSQS client, String serviceEndpoint, String queueName) {
        this.client = client;
        this.queueName = queueName;
        this.queueUrl = serviceEndpoint + "/queue/" + queueName;
        this.mapper = new ObjectMapper();
    }

    public String getQueueUrl() {
        return queueUrl;
    }

    public QueueSize getQueueSize() {
        GetQueueAttributesResult queueAttributes = client
                .getQueueAttributes(queueUrl, Collections.singletonList("All"));
        int available = Integer.parseInt(queueAttributes.getAttributes().get("ApproximateNumberOfMessages"));
        int notVisible = Integer.parseInt(queueAttributes.getAttributes().get("ApproximateNumberOfMessagesNotVisible"));
        int delayed = Integer.parseInt(queueAttributes.getAttributes().get("ApproximateNumberOfMessagesDelayed"));
        return new QueueSize(available, notVisible, delayed);
    }

    public void send(Object object) throws EventSerializationException {
        QueueEvent annotation = object.getClass().getAnnotation(QueueEvent.class);
        if (annotation == null) {
            throw new EventSerializationException(object.getClass()+" not a QueueEvent");
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
            throw new EventSerializationException("Failed to serialize event of type "+object.getClass(), e);
        }
    }

    public void purge() throws Exception {
        logToConsole("        SqsEventQueue purge [start]");
        try {
            client.purgeQueue(new PurgeQueueRequest(queueUrl));
        } catch (Exception e) {
            logToConsole("        SqsEventQueue purge [error]");
            throw new Exception("Local SQS queue not running", e);
        }
        logToConsole("        SqsEventQueue purge [end]");
    }

    public void logToConsole(String s) {
        if ((System.getenv("DEBUG") != null) && System.getenv("DEBUG").contains("true")) {
            System.out.println(s);
        }
    }

    public String getName() {
        return queueName;
    }

    public long getSize() {
        return getQueueSize().getAvailable();
    }
}
