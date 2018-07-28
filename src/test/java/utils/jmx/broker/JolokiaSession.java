package utils.jmx.broker;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.util.Map;

/**
 * Created by julianghionoiu on 27/09/2015.
 */
class JolokiaSession {
    private final CloseableHttpClient httpClient;
    private final Gson gson;

    private final URI jolokiaURI;

    private JolokiaSession(URI jolokiaURI) {
        this.jolokiaURI = jolokiaURI;
        this.httpClient = HttpClients.createDefault();
        this.gson = new Gson();
    }

    static JolokiaSession connect(String host, int adminPort) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        URI jolokiaURI = URI.create("http://" + host + ":" + adminPort + "/api/jolokia");

        HttpGet httpGet = new HttpGet(jolokiaURI.resolve("/api/jolokia/version"));
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            String content = EntityUtils.toString(response.getEntity());

            Gson gson = new Gson();
            JsonElement jsonElement = gson.fromJson(content, JsonElement.class);
            JsonElement value = jsonElement.getAsJsonObject().get("value");
            String jolokiaVersion = value.getAsJsonObject().getAsJsonPrimitive("agent").getAsString();


            String expectedJolokiaVersion = "1.2.2";
            if (!expectedJolokiaVersion.equals(jolokiaVersion)) {
                throw new Exception(String.format("Failed to retrieve the right Jolokia version. Expected: %s got %s",
                        expectedJolokiaVersion, jolokiaVersion));
            }
        }

        httpClient.close();
        return new JolokiaSession(jolokiaURI);
    }

    JsonElement request(Map<String, Object> jolokiaPayload) throws Exception {
        String jsonPayload = gson.toJson(jolokiaPayload);
        HttpPost httpPost = new HttpPost(jolokiaURI);
        httpPost.addHeader("Content-Type", "application/json");
        httpPost.setEntity(new StringEntity(jsonPayload));

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int statusCode = response.getStatusLine().getStatusCode();
            String content = EntityUtils.toString(response.getEntity());

            if (statusCode != 200) {
                throw new Exception(String.format("Failed Jolokia call: %d: %s", statusCode, content));
            }

            JsonElement jsonElement = gson.fromJson(content, JsonElement.class);
            return jsonElement.getAsJsonObject().get("value");
        }
    }

}
