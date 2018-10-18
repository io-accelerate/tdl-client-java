package acceptance.sqs.runner;

import com.google.gson.*;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.lang.reflect.Type;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;


class WiremockProcess {
    private final String hostname;
    private final int port;

    WiremockProcess(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    void createNewMapping(RunnerSteps.ServerConfig config) throws UnirestException {
        final Gson gson = new GsonBuilder().registerTypeAdapter(RunnerSteps.ServerConfig.class, new ServerConfigSerialiser()).create();
        String json = gson.toJson(config);

        String url = String.format("http://%s:%d/%s", hostname, port, "__admin/mappings/new");
        Unirest.post(url).body(json).asJson();
    }

    void reset() throws UnirestException {
        String url = String.format("http://%s:%d/%s", hostname, port, "__admin/reset");
        Unirest.post(url).asJson();
    }


    @SuppressWarnings("SameParameterValue")
    void verifyEndpointWasHit(String endpoint, String methodType, String body) throws UnirestException {
        String failMessage = "Endpoint \"" + endpoint + "\" should have been hit exactly once, with methodType \"" + methodType + "\"";
        assertThat(failMessage, countRequestsWithEndpoint(endpoint, methodType, body), equalTo(1));
    }

    private int countRequestsWithEndpoint(String endpoint, String verb, String body) throws UnirestException {
        String url = String.format("http://%s:%d/%s", hostname, port, "__admin/requests/count");
        RequestMatchingData request = new RequestMatchingData();
        request.verb = verb;
        request.url = endpoint;
        request.equalTo = body;

        final Gson gson = new GsonBuilder().registerTypeAdapter(RequestMatchingData.class, new RequestMatchingSerialiser()).create();
        String json = gson.toJson(request);

        HttpResponse<JsonNode> response = Unirest.post(url).body(json).asJson();
        return response.getBody().getObject().getInt("count");
    }

    private static class RequestMatchingData {
        String url;
        String urlPattern;
        String verb;
        String equalTo;
    }

    static class RequestMatchingSerialiser implements JsonSerializer<RequestMatchingData> {

        @Override
        public JsonElement serialize(final RequestMatchingData request, final Type typeOfSrc, final JsonSerializationContext context) {
            final JsonObject requestJsonObj = new JsonObject();

            if (request.equalTo != null) {
                JsonArray bodyPatterns = new JsonArray();
                final JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("equalTo", request.equalTo);
                bodyPatterns.add(jsonObject);
                requestJsonObj.add("bodyPatterns", bodyPatterns);
            }
            if (request.url != null) {
                requestJsonObj.addProperty("url", request.url);
            }
            if (request.urlPattern != null) {
                requestJsonObj.addProperty("urlPattern", request.urlPattern);
            }
            requestJsonObj.addProperty("method", request.verb);

            final JsonObject completeJson = new JsonObject();
            completeJson.add("request", requestJsonObj);
            return requestJsonObj;
        }
    }

    static class ServerConfigSerialiser implements JsonSerializer<RunnerSteps.ServerConfig> {

        @Override
        public JsonElement serialize(final RunnerSteps.ServerConfig data, final Type typeOfSrc, final JsonSerializationContext context) {
            final JsonObject requestJsonObj = new JsonObject();
            if (data.endpointMatches != null) {
                requestJsonObj.addProperty("urlPattern", data.endpointMatches);
            }
            if (data.endpointEquals != null) {
                requestJsonObj.addProperty("url", data.endpointEquals);
            }

            requestJsonObj.addProperty("method", data.verb);

            if (data.acceptHeader != null) {
                final JsonObject headerJsonObj = new JsonObject();
                final JsonObject acceptJsonObj = new JsonObject();
                acceptJsonObj.addProperty("contains", data.acceptHeader);
                headerJsonObj.add("Accept", acceptJsonObj);
                requestJsonObj.add("headers", headerJsonObj);
            }

            final JsonObject responseJsonObj = new JsonObject();

            if (data.responseBody != null) {
                responseJsonObj.addProperty("body", data.responseBody);
            }

            if (data.statusMessage != null) {
                responseJsonObj.addProperty("statusMessage", data.statusMessage);
            }

            responseJsonObj.addProperty("status", data.status);

            final JsonObject completeJson = new JsonObject();
            completeJson.add("response", responseJsonObj);
            completeJson.add("request", requestJsonObj);

            return completeJson;
        }
    }
}
