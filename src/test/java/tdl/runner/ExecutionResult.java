package tdl.runner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by julianghionoiu on 05/09/2015.
 */
public class ExecutionResult {
    private final List<String> stdout;
    private final List<String> stderr;
    private final int exitCode;

    //Debt: Add tests for this class contract

    public ExecutionResult(List<String> stdout, List<String> stderr, int exitCode) {
        this.stdout = stdout;
        this.stderr = stderr;
        this.exitCode = exitCode;
    }

    public static ExecutionResult fromRunningProcess(Process process) throws IOException, InterruptedException {
        List<String> processStdout = readStreamToList(process.getInputStream());
        List<String> processStderr = readStreamToList(process.getErrorStream());
        process.waitFor();
        int exitCode = process.exitValue();
        return new ExecutionResult(processStdout, processStderr, exitCode);
    }


    public List<String> getStdout() {
        return stdout;
    }

    public List<String> getStderr() {
        return stderr;
    }

    public int getExitCode() {
        return exitCode;
    }

    public boolean isFailed() {
        return exitCode != 0;
    }


    //~~~ Utilities

    private static List<String> readStreamToList(InputStream is) throws IOException {
        List<String> standardOutput = new LinkedList<>();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;
        while ((line = br.readLine()) != null) {
            standardOutput.add(line);
        }
        return standardOutput;
    }
}

