package tdl.oldrunner;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class SingletonTestServer {
    private static final String HOSTNAME = "localhost";
    private static final int PORT = 8222;
    private static WireMockServer SERVER_INSTANCE;

    public SingletonTestServer() {
        if (SERVER_INSTANCE == null) {
            WireMock.configureFor(HOSTNAME, PORT);
            SERVER_INSTANCE = new WireMockServer(wireMockConfig().port(8222));
            SERVER_INSTANCE.start();
        }
    }

    public void resetMappings() {
        SERVER_INSTANCE.resetMappings();
    }
}
