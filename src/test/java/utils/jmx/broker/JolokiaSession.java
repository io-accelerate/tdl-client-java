package utils.jmx.broker;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
        Gson gson = new Gson();
        URI jolokiaURI = URI.create("http://" + host + ":" + adminPort + "/api/jolokia");
        URI versionURI = URI.create(jolokiaURI.toString() + "/version");

        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(versionURI)
                    .header("Origin", "http://localhost")
                    .GET()
                    .build();

            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }
        JsonObject json = gson.fromJson(response.body(), JsonObject.class);
        JsonObject value = json.getAsJsonObject("value");

        if (value == null || !value.has("agent") || value.get("agent").getAsString().isEmpty()) {
            throw new Exception("Failed to retrieve the right Jolokia version.");
        }
        
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
