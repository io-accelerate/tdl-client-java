package tdl.client.runner;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import static tdl.client.runner.RunnerAction.deployToProduction;

class RecordingSystem implements RoundChangesListener {
    private static final String RECORDING_SYSTEM_ENDPOINT = "http://localhost:41375";
    private boolean recordingRequired;

    RecordingSystem(boolean recordingRequired) {
        this.recordingRequired = recordingRequired;
    }

    private boolean isRecordingRequired() {
        return recordingRequired;
//        return Boolean.parseBoolean(readFromConfigFile("tdl_require_rec", "true"));
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

    private void notifyEvent(String lastFetchedRound, String shortName) {
        if (!recordingRequired) {
            return;
        }

        try {
            HttpResponse<String> stringHttpResponse = Unirest.post(RECORDING_SYSTEM_ENDPOINT + "/notify")
                    .body(lastFetchedRound+"/"+shortName)
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

    void deployNotifyEvent(String lastFetchedRound) {
        notifyEvent(lastFetchedRound, deployToProduction.getShortName());
    }

    @Override
    public void onNewRound(String roundId, String shortName) {
        notifyEvent(roundId, shortName);
    }
}

