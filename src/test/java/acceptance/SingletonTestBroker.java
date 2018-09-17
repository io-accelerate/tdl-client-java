package acceptance;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import tdl.client.runner.connector.SqsEventQueue;

/**
 * Created by julianghionoiu on 11/10/2015.
 */
public class SingletonTestBroker {
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
        logToConsole(" SingletonTestBroker creation [end]");
    }

    public SqsEventQueue addQueue(String queueName) throws Exception {
        logToConsole(" SingletonTestBroker addQueue");
        return new SqsEventQueue(client, serviceEndpoint, queueName);

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
}
