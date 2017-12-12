package tdl.oldrunner;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;


import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class SingletonTestServer {
    // Test broker admin connection
    private static final String HOSTNAME = "localhost";
    private static final int JMX_PORT = 28161;
    private static final String BROKER_NAME = "TEST.BROKER";

    private static WireMockServer SERVER_INSTANCE;

    public SingletonTestServer() throws Exception {
        //All the instances are just a proxy for the same broker
        if (SERVER_INSTANCE == null) {
            WireMock.configureFor("localhost", 8222);
            SERVER_INSTANCE = new WireMockServer(wireMockConfig().port(8222));
            SERVER_INSTANCE.start();
        }
    }

    public WireMockServer getServer() {
        return SERVER_INSTANCE;
    }

    public void resetMappings() {
        SERVER_INSTANCE.resetMappings();
    }
}
