package tdl.oldrunner;

import com.google.gson.*;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.lang.reflect.Type;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class WiremockProcess {
    private static final String HOSTNAME = "localhost";
    private static final int PORT = 8222;
    private static final String anyUnicodeRegex = "(?:\\P{M}\\p{M}*)+";

    public static void createGetStubMappingForEndpointWithBody(String endpoint, String body) throws UnirestException {
        createStubMappingForEndpointWithBody(endpoint, body, "GET");
    }

    public static void createPostStubMappingForEndpointWithBody(String endpoint, String body) throws UnirestException {
        createStubMappingForEndpointWithBody(endpoint, body, "POST");
    }

    private static void createStubMappingForEndpointWithBody(String endpoint, String body, String methodType) throws UnirestException {
        // check if process running, otherwise fail.
        RequestData requestData = new RequestData();
        requestData.request = new RequestData.Request();
        requestData.request.method = methodType;
        requestData.request.urlPattern = String.format("/%s/%s", endpoint, anyUnicodeRegex);
        requestData.response = new RequestData.Response();
        requestData.response.status = 200;
        requestData.response.body = body;

        final Gson gson = new GsonBuilder().registerTypeAdapter(RequestData.class, new RequestDataSerialiser()).create();
        String json = gson.toJson(requestData);

        String url = String.format("http://%s:%d/%s", HOSTNAME, PORT, "__admin/mappings/new");
        Unirest.post(url).body(json).asJson();
    }

    static void verifyGetEndpointWasHit(String endpoint) throws UnirestException {
        assertThat(countRequestsWithGetEndpoint(endpoint), equalTo(1));
    }

    static int countRequestsWithGetEndpoint(String endpoint) throws UnirestException {
        return countRequestsWithEndpoint(endpoint, "GET");
    }

    static int countRequestsWithPostEndpoint(String endpoint) throws UnirestException {
        return countRequestsWithEndpoint(endpoint, "POST");
    }

    private static int countRequestsWithEndpoint(String endpoint, String methodType) throws UnirestException {
        String url = String.format("http://%s:%d/%s", HOSTNAME, PORT, "__admin/requests/count");
        RequestData.Request request = new RequestData.Request();
        request.method = methodType;
        request.urlPattern = String.format("/%s/%s", endpoint, anyUnicodeRegex);

        final Gson gson = new GsonBuilder().registerTypeAdapter(RequestData.Request.class, new RequestSerialiser()).create();
        String json = gson.toJson(request);

        HttpResponse<JsonNode> response = Unirest.post(url).body(json).asJson();
        return response.getBody().getObject().getInt("count");
    }

    public static void verifyPostEndpointWasHit(String endpoint) throws UnirestException {
        assertThat("Endpoint \"" + endpoint + "\" should have been hit exactly once.", 1, equalTo(countRequestsWithPostEndpoint(endpoint)));
    }

    public static void reset() throws UnirestException {
        String url = String.format("http://%s:%d/%s", HOSTNAME, PORT, "__admin/reset");
        Unirest.post(url).asJson();
    }

    private static class RequestData {
        Request request;
        Response response;

        public static class Request {
            String urlPattern;
            String method;
        }

        public static class Response {
            int status;
            String body;
        }
    }

    public static class RequestDataSerialiser implements JsonSerializer<RequestData> {

        @Override
        public JsonElement serialize(final RequestData requestData, final Type typeOfSrc, final JsonSerializationContext context) {
            final JsonObject requestJsonObj = new JsonObject();
            requestJsonObj.addProperty("urlPattern", requestData.request.urlPattern);
            requestJsonObj.addProperty("method", requestData.request.method);

            final JsonObject responseJsonObj = new JsonObject();
            responseJsonObj.addProperty("status", requestData.response.status);
            responseJsonObj.addProperty("body", requestData.response.body);

            final JsonObject completeJson = new JsonObject();
            completeJson.add("response", responseJsonObj);
            completeJson.add("request", requestJsonObj);

            return completeJson;
        }
    }

    public static class RequestSerialiser implements JsonSerializer<RequestData.Request> {

        @Override
        public JsonElement serialize(final RequestData.Request request, final Type typeOfSrc, final JsonSerializationContext context) {
            final JsonObject requestJsonObj = new JsonObject();
            requestJsonObj.addProperty("urlPattern", request.urlPattern);
            requestJsonObj.addProperty("method", request.method);
            return requestJsonObj;
        }
    }
}
