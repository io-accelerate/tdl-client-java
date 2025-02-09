package io.accelerate.client.runner;

import io.accelerate.client.audit.AuditStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

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
            Files.write(descriptionPath, description.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        auditStream.println("Challenge description saved to file: " + descriptionPath + ".");

        //Save round label
        try {
            Files.write(LAST_FETCHED_ROUND_PATH, label.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    static String getLastFetchedRound() {
        try {
            try (Stream<String> s = Files.lines(LAST_FETCHED_ROUND_PATH)) {
                return s.findFirst().orElse("noRound");
            }
        } catch (IOException e) {
            return "noRound";
        }
    }
}
