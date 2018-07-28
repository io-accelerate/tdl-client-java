package tdl.client.runner;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

class RecordingSystem implements RoundChangesListener {


    enum Event {
        ROUND_START("new"),
        ROUND_SOLUTION_DEPLOY("deploy"),
        ROUND_COMPLETED("done");

        private String name;

        Event(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private static final String RECORDING_SYSTEM_ENDPOINT = "http://localhost:41375";
    private boolean recordingRequired;

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
            HttpResponse<String> stringHttpResponse = Unirest.get(RECORDING_SYSTEM_ENDPOINT + "/status").asString();
            if (stringHttpResponse.getStatus() == 200 && stringHttpResponse.getBody().startsWith("OK")) {
                return true;
            }
        } catch (UnirestException e) {
            System.err.println("Could not reach recording system: " + e.getMessage());
        }

        return false;
    }

    public void notifyEvent(String roundId, Event event) {
        sentPost("/notify", roundId+"/"+event.getName());
    }

    public void tellToStop() {
        sentPost("/stop", "");
    }

    private void sentPost(String endpoint, String body) {
        if (!recordingRequired) {
            return;
        }

        try {
            HttpResponse<String> stringHttpResponse = Unirest.post(RECORDING_SYSTEM_ENDPOINT + endpoint)
                    .body(body)
                    .asString();
            if (stringHttpResponse.getStatus() != 200) {
                System.err.println("Recording system returned code: "+stringHttpResponse.getStatus());
                return;
            }

            if (!stringHttpResponse.getBody().startsWith("ACK")) {
                System.err.println("Recording system returned body: "+stringHttpResponse.getStatus());
            }
        } catch (UnirestException e) {
            System.err.println("Could not reach recording system: " + e.getMessage());
        }
    }

    @Override
    public void onNewRound(String roundId) {
        notifyEvent(roundId, Event.ROUND_START);
    }
}

