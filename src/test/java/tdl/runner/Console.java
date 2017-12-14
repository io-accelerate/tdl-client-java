package tdl.runner;

import com.google.common.collect.Lists;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

import java.io.*;
import java.util.List;


class Console {
    private static final InteractionLogger INTERACTION_LOG = InteractionLogger.INSTANCE;
    private final BufferedReader reader;
    private final BufferedWriter writer;

    private Console(BufferedReader reader, BufferedWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    static Console from(AppThread appThread) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(appThread.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(appThread.getOutputStream()));
        return new Console(reader, writer);
    }


    //~~~~ Read

    String readLineThat(Matcher<String> matcher) throws IOException, InteractionException {
        String value = reader.readLine();
        INTERACTION_LOG.received(value);

        ensureValueMatches(matcher, value);
        return value;
    }

    List<String> readLinesUntilLine(Matcher<String> matcher) throws IOException, InteractionException {
        List<String> lines =  Lists.newArrayList();
        String line = "";

        while((line != null) &&
                (!matcher.matches(line))) {
            line = reader.readLine();
            lines.add(line);
            INTERACTION_LOG.received(line);
        }

        return lines;
    }

    //~~~~ Write

    void pressEnter() throws IOException {
        write("\n");
    }

    void write(String str) throws IOException {
        writer.write(str);
        writer.flush();

        INTERACTION_LOG.sent(str);
    }

    void writeLine(String str) throws IOException {
        writer.write(str + "\n");
        writer.flush();

        INTERACTION_LOG.sent(str);
    }

    //~~~~~ Utils

    private void ensureValueMatches(Matcher<String> matcher, String value) throws InteractionException {
        if (!matcher.matches(value)) {
            Description description = new StringDescription();
            description.appendText("Failed to read expected value:");
            description.appendText("\nExpected ");
            matcher.describeTo(description);
            description.appendText("\n        ");
            matcher.describeMismatch(value, description);

            throw new InteractionException(description.toString());
        }
    }
}

