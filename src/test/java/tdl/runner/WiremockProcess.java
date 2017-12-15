package tdl.runner;

import com.google.gson.*;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


class WiremockProcess {
    private final String hostname;
    private final int port;
    private List<RequestData> configData = new ArrayList<>();
    private static final String anyUnicodeRegex = "(?:\\P{M}\\p{M}*)+";

    WiremockProcess(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    void createStubMapping(String verb, String endpoint, int returnCode, String body) {
        // Obvs - change public fields to methods

        RequestData data = new RequestData();
        data.request = new RequestData.Request();
        data.request.verb = verb;
        String urlPattern = getUrlPattern(endpoint);
        data.request.urlPattern = urlPattern;
        data.response = new RequestData.Response();
        data.response.status = returnCode;
        data.response.body = body;
        configData.add(data);
    }

    private String getUrlPattern(String endpoint) {
        return String.format("/%s/%s", endpoint, anyUnicodeRegex);
    }

    void addHeaderToStubs(String header) throws UnirestException {
        for (RequestData data : configData) {
            data.request.headers = new RequestData.Request.Headers();
            data.request.headers.accept = header;
        }
    }

    void adjustStubMappingResponse(String endpoint, String response) {
        Optional<RequestData> data = configData.stream().filter(s -> s.request.urlPattern.contains(endpoint)).findFirst();
        if (data.isPresent()) {
            RequestData requestData = data.get();
            requestData.request.urlPattern = getUrlPattern(endpoint);
            requestData.response.body = response;
        }
    }

    void configureServer() throws UnirestException {
        for (RequestData data : configData) {
            final Gson gson = new GsonBuilder().registerTypeAdapter(RequestData.class, new RequestDataSerialiser()).create();
            String json = gson.toJson(data);

            String url = String.format("http://%s:%d/%s", hostname, port, "__admin/mappings/new");
            Unirest.post(url).body(json).asJson();
        }
    }

    void reset() throws UnirestException {
        String url = String.format("http://%s:%d/%s", hostname, port, "__admin/reset");
        Unirest.post(url).asJson();
    }

    private static class RequestData {
        Request request;
        Response response;

        static class Request {
            String urlPattern;
            String verb;
            Headers headers;

            static class Headers {
                String accept;
            }
        }

        static class Response {
            int status;
            String body;
        }
    }

    public static class RequestDataSerialiser implements JsonSerializer<RequestData> {

        @Override
        public JsonElement serialize(final RequestData requestData, final Type typeOfSrc, final JsonSerializationContext context) {
            RequestSerialiser requestSerialiser = new RequestSerialiser();
            final JsonElement requestJsonObj = requestSerialiser.serialize(requestData.request, typeOfSrc, context);

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
            requestJsonObj.addProperty("method", request.verb);

            final JsonObject headerJsonObj = new JsonObject();
            final JsonObject acceptJsonObj = new JsonObject();
            acceptJsonObj.addProperty("contains", request.headers.accept);
            headerJsonObj.add("Accept", acceptJsonObj);
            requestJsonObj.add("headers", headerJsonObj);

            return requestJsonObj;
        }
    }
}
