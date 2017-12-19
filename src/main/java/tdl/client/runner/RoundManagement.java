package tdl.client.runner;

import com.google.common.io.Files;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

import static tdl.client.runner.RunnerAction.getNewRoundDescription;

public class RoundManagement {
    private static final Path CHALLENGES_FOLDER = Paths.get("challenges");
    private static final Path LAST_FETCHED_ROUND_PATH = CHALLENGES_FOLDER.resolve("XR.txt");

    static void saveDescription(RoundChangesListener listener, String rawDescription, IConsoleOut consoleOut) {
        // DEBT - the first line of the response is the ID for the round, the rest of the responseMessage is the description
        int newlineIndex = rawDescription.indexOf('\n');
        if (newlineIndex <= 0) return;

        String roundId = rawDescription.substring(0, newlineIndex);
        String lastFetchedRound = getLastFetchedRound();
        if (!roundId.equals(lastFetchedRound)) {
            listener.onNewRound(roundId, getNewRoundDescription.getShortName());
        }
        saveDescription(roundId, rawDescription, consoleOut);
    }

    public static String saveDescription(String label, String description, IConsoleOut consoleOut) {
        //Save description
        Path descriptionPath = CHALLENGES_FOLDER.resolve(label + ".txt");
        try {
            Files.write(description.getBytes(), descriptionPath.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        consoleOut.println("Challenge description saved to file: " + descriptionPath + ".");

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
