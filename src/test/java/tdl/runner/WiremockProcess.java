package tdl.runner;

import com.google.gson.*;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.lang.reflect.Type;


class WiremockProcess {
    private final String hostname;
    private final int port;

    WiremockProcess(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    void createNewMapping(Steps.ServerConfig config) throws UnirestException {
        final Gson gson = new GsonBuilder().registerTypeAdapter(Steps.ServerConfig.class, new ServerConfigSerialiser()).create();
        String json = gson.toJson(config);

        String url = String.format("http://%s:%d/%s", hostname, port, "__admin/mappings/new");
        Unirest.post(url).body(json).asJson();
    }

    void reset() throws UnirestException {
        String url = String.format("http://%s:%d/%s", hostname, port, "__admin/reset");
        Unirest.post(url).asJson();
    }

    public static class ServerConfigSerialiser implements JsonSerializer<Steps.ServerConfig> {

        @Override
        public JsonElement serialize(final Steps.ServerConfig data, final Type typeOfSrc, final JsonSerializationContext context) {
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

            if (data.returnBody != null) {
                responseJsonObj.addProperty("body", data.returnBody);
            }

            responseJsonObj.addProperty("status", data.returnStatus);

            final JsonObject completeJson = new JsonObject();
            completeJson.add("response", responseJsonObj);
            completeJson.add("request", requestJsonObj);

            return completeJson;
        }
    }
}
