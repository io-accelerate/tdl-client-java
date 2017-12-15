package tdl.client.runner;

import com.google.common.io.Files;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

import static tdl.client.runner.RunnerAction.getNewRoundDescription;

class RoundManagement {
    private static final Path CHALLENGES_FOLDER = Paths.get("challenges");
    private static final Path LAST_FETCHED_ROUND_PATH = CHALLENGES_FOLDER.resolve("XR.txt");

    static void saveDescription(String rawDescription, PrintStream writer) {
        // DEBT - the first line of the response is the ID for the round, the rest of the responseMessage is the description
        int newlineIndex = rawDescription.indexOf('\n');
        if (newlineIndex <= 0) return;

        String roundId = rawDescription.substring(0, newlineIndex);
        String lastFetchedRound = getLastFetchedRound();
        if (!roundId.equals(lastFetchedRound)) {
            RecordingSystem.notifyEvent(roundId, getNewRoundDescription.getShortName(), writer);
        }
        saveDescription(roundId, rawDescription, writer);
    }

    static String saveDescription(String label, String description, PrintStream writer) {
        //Save description
        Path descriptionPath = CHALLENGES_FOLDER.resolve(label + ".txt");
        try {
            Files.write(description.getBytes(), descriptionPath.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        writer.println("Challenge description saved to file: " + descriptionPath + ".");

        //Save round label
        try {
            Files.write(label.getBytes(), LAST_FETCHED_ROUND_PATH.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return "OK";
    }

    static String getLastFetchedRound() {
        try {
            return Files.readFirstLine(LAST_FETCHED_ROUND_PATH.toFile(), Charset.defaultCharset());
        } catch (IOException e) {
            return "noRound";
        }
    }
}
