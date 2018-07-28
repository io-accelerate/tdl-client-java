package tdl.client.runner;

import com.google.common.io.Files;
import tdl.client.audit.AuditStream;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

class RoundManagement {
    private static final Path CHALLENGES_FOLDER = Paths.get("challenges");
    private static final Path LAST_FETCHED_ROUND_PATH = CHALLENGES_FOLDER.resolve("XR.txt");

    static void saveDescription(RoundChangesListener listener, String rawDescription, AuditStream auditStream) {
        // DEBT - the first line of the response is the ID for the round, the rest of the responseMessage is the description
        int newlineIndex = rawDescription.indexOf('\n');
        if (newlineIndex <= 0) return;

        String roundId = rawDescription.substring(0, newlineIndex);
        String lastFetchedRound = getLastFetchedRound();
        if (!roundId.equals(lastFetchedRound)) {
            listener.onNewRound(roundId);
        }
        saveDescription(roundId, rawDescription, auditStream);
    }

    private static void saveDescription(String label, String description, AuditStream auditStream) {
        File challengesFolder = CHALLENGES_FOLDER.toFile();
        if (!challengesFolder.exists()) {
            //noinspection ResultOfMethodCallIgnored
            challengesFolder.mkdir();
        }
        //Save description

        Path descriptionPath = CHALLENGES_FOLDER.resolve(label + ".txt");
        try {
            Files.write(description.getBytes(), descriptionPath.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        auditStream.println("Challenge description saved to file: " + descriptionPath + ".");

        //Save round label
        try {
            Files.write(label.getBytes(), LAST_FETCHED_ROUND_PATH.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    static String getLastFetchedRound() {
        try {
            return Files.readFirstLine(LAST_FETCHED_ROUND_PATH.toFile(), Charset.defaultCharset());
        } catch (IOException e) {
            return "noRound";
        }
    }
}
