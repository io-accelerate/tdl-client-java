package utils.jmx.broker;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * Created by julianghionoiu on 27/09/2015.
 */
class JolokiaSession {
    private final HttpClient httpClient;
    private final Gson gson;

    private final URI jolokiaURI;

    private JolokiaSession(URI jolokiaURI) {
        this.jolokiaURI = jolokiaURI;
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    static JolokiaSession connect(String host, int adminPort) throws Exception {
        URI jolokiaURI = URI.create("http://" + host + ":" + adminPort + "/api/jolokia");
        
        return new JolokiaSession(jolokiaURI);
    }

    JsonElement request(Map<String, Object> jolokiaPayload) throws Exception {
        String jsonPayload = gson.toJson(jolokiaPayload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(jolokiaURI)
                .header("Content-Type", "application/json")
                .header("Origin", "http://localhost")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());


        int statusCode = response.statusCode();
        String content = response.body();

        if (statusCode != 200) {
            throw new Exception(String.format("Failed Jolokia call: %d: %s", statusCode, content));
        }

        JsonElement jsonElement = gson.fromJson(content, JsonElement.class);
        return jsonElement.getAsJsonObject().get("value");
    }

}
