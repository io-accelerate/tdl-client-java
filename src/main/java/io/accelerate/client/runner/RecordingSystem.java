package io.accelerate.client.runner;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

class RecordingSystem implements RoundChangesListener {


    enum Event {
        ROUND_START("new"),
        ROUND_SOLUTION_DEPLOY("deploy"),
        ROUND_COMPLETED("done");

        private final String name;

        Event(String name) {
            this.name = name;
        }

        String getName() {
            return name;
        }
    }

    private static final String RECORDING_SYSTEM_ENDPOINT = "http://localhost:41375";
    private final boolean recordingRequired;

    RecordingSystem(boolean recordingRequired) {
        this.recordingRequired = recordingRequired;
    }

    private boolean isRecordingRequired() {
        return recordingRequired;
    }

    boolean isRecordingSystemOk() {
        //noinspection SimplifiableConditionalExpression
        return isRecordingRequired() ? isRunning() : true;
    }

    private static boolean isRunning() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RECORDING_SYSTEM_ENDPOINT + "/status"))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && response.body().startsWith("OK")) {
                return true;
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Could not reach recording system: " + e.getMessage());
        }

        return false;
    }

    void notifyEvent(String roundId, Event event) {
        System.out.println("Notify round "+roundId+", event "+event.getName());
        sendPost("/notify", roundId+"/"+event.getName());
    }

    void tellToStop() {
        System.out.println("Stopping recording system");
        sendPost("/stop", "");
    }

    private void sendPost(String endpoint, String body) {
        if (!recordingRequired) {
            return;
        }

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RECORDING_SYSTEM_ENDPOINT + endpoint))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("Recording system returned code: "+response.statusCode());
                return;
            }

            if (!response.body().startsWith("ACK")) {
                System.err.println("Recording system returned body: "+response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Could not reach recording system: " + e.getMessage());
        }
    }

    @Override
    public void onNewRound(String roundId) {
        notifyEvent(roundId, Event.ROUND_START);
    }
}

