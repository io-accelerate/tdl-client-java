package tdl.client.jms.runner;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


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
            String encodedPath = URLEncoder.encode(this.journeyId, "UTF-8");
            String url = String.format("http://%s:%d/%s/%s", this.hostname, port, name, encodedPath);
            HttpResponse<String> response = Unirest.get(url)
                    .header("Accept", this.acceptHeader)
                    .header("Accept-Charset", "UTF-8")
                    .asString();
            ensureStatusOk(response);
            return response.getBody();
        } catch (UnirestException | UnsupportedEncodingException e ) {
            throw new OtherCommunicationException("Could not perform GET request",e);
        }
    }

    //~~~~~~~ POST ~~~~~~~~

    String sendAction(String action) throws
            ClientErrorException, ServerErrorException, OtherCommunicationException {
        try {
            String encodedPath = URLEncoder.encode(this.journeyId, "UTF-8");
            String url = String.format("http://%s:%d/action/%s/%s", this.hostname, port, action, encodedPath);
            HttpResponse<String> response =  Unirest.post(url)
                    .header("Accept", this.acceptHeader)
                    .header("Accept-Charset", "UTF-8")
                    .asString();
            ensureStatusOk(response);
            return response.getBody();
        } catch (UnirestException | UnsupportedEncodingException e ) {
            throw new OtherCommunicationException("Could not perform POST request",e);
        }
    }


    //~~~~~~~ Error handling ~~~~~~~~~

    private static void ensureStatusOk(HttpResponse<String> response) throws ClientErrorException,
            ServerErrorException, OtherCommunicationException {
        int responseStatus = response.getStatus();
        if (isClientError(responseStatus)) {
            throw new ClientErrorException(response.getBody());
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

