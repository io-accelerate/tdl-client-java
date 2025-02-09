package io.accelerate.client.runner;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;


class ChallengeServerClient {
    private final String hostname;
    private final String journeyId;
    private final int port;
    private final String acceptHeader;

    ChallengeServerClient(String hostname, int port, String journeyId, boolean useColours) {
        this.hostname = hostname;
        this.port = port;
        this.journeyId = journeyId;
        this.acceptHeader = useColours ? "text/coloured" : "text/not-coloured";
    }

    //~~~~~~~ GET ~~~~~~~~

    String getJourneyProgress() throws OtherCommunicationException, ServerErrorException, ClientErrorException {
        return get("journeyProgress");
    }

    String getAvailableActions() throws OtherCommunicationException, ServerErrorException, ClientErrorException {
        return get("availableActions");
    }

    String getRoundDescription() throws OtherCommunicationException, ServerErrorException, ClientErrorException {
        return get("roundDescription");
    }

    private String get(String name) throws OtherCommunicationException, ServerErrorException, ClientErrorException {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String encodedPath = URLEncoder.encode(this.journeyId, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("http://%s:%d/%s/%s", this.hostname, port, name, encodedPath)))
                    .header("Accept", this.acceptHeader)
                    .header("Accept-Charset", "UTF-8")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            ensureStatusOk(response);

            return response.body();
        } catch (IOException | InterruptedException e ) {
            throw new OtherCommunicationException("Could not perform GET request",e);
        }
    }

    //~~~~~~~ POST ~~~~~~~~

    String sendAction(String action) throws
            ClientErrorException, ServerErrorException, OtherCommunicationException {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String encodedPath = URLEncoder.encode(this.journeyId, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("http://%s:%d/action/%s/%s", this.hostname, port, action, encodedPath)))
                    .header("Accept", this.acceptHeader)
                    .header("Accept-Charset", "UTF-8")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();


            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            ensureStatusOk(response);
            return response.body();
        } catch (IOException | InterruptedException e ) {
            throw new OtherCommunicationException("Could not perform POST request",e);
        }
    }


    //~~~~~~~ Error handling ~~~~~~~~~

    private static void ensureStatusOk(HttpResponse<String> response) throws ClientErrorException,
            ServerErrorException, OtherCommunicationException {
        int responseStatus = response.statusCode();
        if (isClientError(responseStatus)) {
            throw new ClientErrorException(response.body());
        } else if (isServerError(responseStatus)) {
            throw new ServerErrorException();
        } else if (isOtherErrorResponse(responseStatus)) {
            throw new OtherCommunicationException();
        }
    }

    private static boolean isClientError(int responseStatus) {
        return responseStatus >= 400 && responseStatus < 500;
    }

    private static boolean isServerError(int responseStatus) {
        return responseStatus >= 500 && responseStatus < 600;
    }

    private static boolean isOtherErrorResponse(int responseStatus) {
        return responseStatus < 200 || responseStatus > 300;
    }

    static class ClientErrorException extends Exception {

        private final String responseMessage;
        ClientErrorException(String message) {
            this.responseMessage = message;
        }

        String getResponseMessage() {
            return responseMessage;
        }


    }

    static class ServerErrorException extends Exception {
        ServerErrorException() {
            super();
        }
    }

    static class OtherCommunicationException extends Exception {

        OtherCommunicationException(String message, Exception e) {
            super(message,e);
        }

        OtherCommunicationException() {
            super();
        }
    }
}

