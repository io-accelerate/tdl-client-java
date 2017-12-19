package tdl.runner;

import com.google.gson.*;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.lang.reflect.Type;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

class RecordingServerStub {
    private final String hostname;
    private final int port;
    private WiremockProcess wiremockProcess;

    RecordingServerStub(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
        wiremockProcess = new WiremockProcess(hostname, port);
    }

    void createNewMapping(Steps.ServerConfig config) throws UnirestException {
        wiremockProcess.createNewMapping(config);
    }

    void reset() throws UnirestException {
        wiremockProcess.reset();
    }

    void verifyEndpointWasHit(String endpoint, String methodType, String body) throws UnirestException {
        String failMessage = "Endpoint \"" + endpoint + "\" should have been hit exactly once, with methodType \"" + methodType + "\"";
        assertThat(failMessage, countRequestsWithEndpoint(endpoint, methodType, body), equalTo(1));
    }

    private int countRequestsWithEndpoint(String endpoint, String verb, String body) throws UnirestException {
        String url = String.format("http://%s:%d/%s", hostname, port, "__admin/requests/count");
        RequestMatchingData request = new RequestMatchingData();
        request.verb = verb;
        request.url = endpoint;
        RequestMatchingData.BodyPatterns bodyPattern = new RequestMatchingData.BodyPatterns();
        bodyPattern.equalTo = body;
        request.bodyPatterns = new RequestMatchingData.BodyPatterns[]{bodyPattern};

        final Gson gson = new GsonBuilder().registerTypeAdapter(RequestMatchingData.class, new RequestMatchingSerialiser()).create();
        String json = gson.toJson(request);

        HttpResponse<JsonNode> response = Unirest.post(url).body(json).asJson();
        return response.getBody().getObject().getInt("count");
    }

    private static class RequestMatchingData {
        String url;
        String verb;
        BodyPatterns[] bodyPatterns;
        static class BodyPatterns {
            String equalTo;
        }
    }

    public static class RequestMatchingSerialiser implements JsonSerializer<RequestMatchingData> {

        @Override
        public JsonElement serialize(final RequestMatchingData request, final Type typeOfSrc, final JsonSerializationContext context) {
            JsonArray bodyPatterns = new JsonArray();
            for (RequestMatchingData.BodyPatterns bodyPattern : request.bodyPatterns) {
                JsonElement pattern = new BodyPatternsSerialiser().serialize(bodyPattern, typeOfSrc, context);
                bodyPatterns.add(pattern);
            }
            final JsonObject requestJsonObj = new JsonObject();
            requestJsonObj.addProperty("url", request.url);
            requestJsonObj.addProperty("method", request.verb);
            requestJsonObj.add("bodyPatterns", bodyPatterns);
            return requestJsonObj;
        }
    }

    public static class BodyPatternsSerialiser implements JsonSerializer<RequestMatchingData.BodyPatterns> {

        @Override
        public JsonElement serialize(final RequestMatchingData.BodyPatterns request, final Type typeOfSrc, final JsonSerializationContext context) {
            final JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("equalTo", request.equalTo);

            return jsonObject;
        }
    }
}
