package acceptance.runner;

import acceptance.queue.NoisyImplementationRunner;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.cucumber.java.DataTableType;
import io.cucumber.java.en.*;
import tdl.client.audit.AuditStream;
import tdl.client.queue.ImplementationRunner;
import acceptance.queue.QuietImplementationRunner;
import tdl.client.runner.ActionProvider;
import tdl.client.runner.ChallengeSession;
import tdl.client.runner.ChallengeSessionConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsEqual.equalTo;


public class RunnerSteps {
    private WiremockProcess challengeServerStub;
    private WiremockProcess recordingServerStub;
    private String challengeHostname;
    private int port;
    private final AuditStream auditStream = new TestAuditStream();
    private ImplementationRunner implementationRunner = new QuietImplementationRunner();
    private String implementationRunnerMessage;
    private String journeyId;
    private ActionProvider actionProviderCallback = () -> null;

    // Given

    @Given("There is a challenge server running on \"([^\"]*)\" port (\\d+)$")
    public void setupServerWithSetup(String hostname, int port) throws UnirestException {
        this.challengeHostname = hostname;
        this.port = port;
        challengeServerStub = new WiremockProcess(hostname, port);
        challengeServerStub.reset();
    }

    @And("There is a recording server running on \"([^\"]*)\" port (\\d+)$")
    public void setupRecordingServerWithSetup(String hostname, int port) throws UnirestException {
        recordingServerStub = new WiremockProcess(hostname, port);
        recordingServerStub.reset();
    }

    record ServerConfig (
        String verb,
        String endpointEquals,
        String endpointMatches,
        int status,
        String responseBody,
        String acceptHeader,
        String statusMessage
    ) {}

    @DataTableType
    public ServerConfig serverConfig(Map<String, String> entry) {
        return new ServerConfig(
                entry.getOrDefault("verb", null),
                entry.getOrDefault("endpointEquals", null),
                entry.getOrDefault("endpointMatches", null),
                Integer.parseInt(entry.getOrDefault("status", "0")),
                entry.getOrDefault("responseBody", null),
                entry.getOrDefault("acceptHeader", null),
                entry.getOrDefault("statusMessage", null)
                );
    }


    @And("the challenge server exposes the following endpoints$")
    public void configureChallengeServerEndpoint(List<ServerConfig> configs) throws UnirestException {
        for (ServerConfig config : configs) {
            challengeServerStub.createNewMapping(config);
        }
    }

    @And("the recording server exposes the following endpoints$")
    public void configureRecordingServerEndpoint(List<ServerConfig> configs) throws UnirestException {
        for (ServerConfig config : configs) {
            recordingServerStub.createNewMapping(config);
        }
    }

    @Given("^the challenge server returns (\\d+) for all requests$")
    public void the_challenge_server_returns_for_all_requests(int returnCode) throws Throwable {
        ServerConfig config = new ServerConfig(
                "ANY",
                null,
                "^(.*)",
                returnCode,
                null,
                null,
                null);
        challengeServerStub.createNewMapping(config);
    }

    @Given("^the challenge server returns (\\d+), response body \"([^\"]*)\" for all requests$")
    public void the_challenge_server_returns_response_body_for_all_requests(int returnCode, String body) throws Throwable {
        ServerConfig config = new ServerConfig(
                "ANY",
                null,
                "^(.*)",
                returnCode,
                body,
                null,
                null);
        challengeServerStub.createNewMapping(config);
    }

    @And("the challenges folder is empty")
    public void deleteContentsOfChallengesFolder() {
        Path path = Paths.get("challenges");
        deleteFolderContents(path.toFile());
    }

    private void deleteFolderContents(File folder) {
        File[] files = folder.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolderContents(f);
                } else {
                    //noinspection ResultOfMethodCallIgnored
                    f.delete();
                }
            }
        }
    }

    @Given("the action input comes from a provider returning \"([^\"]*)\"$")
    public void actionInputComesFromProviderReturning(String s) {
        actionProviderCallback = () -> s;
    }

    @Given("^there is an implementation runner that prints \"([^\"]*)\"$")
    public void implementationRunnerPrinter(String s) {
        implementationRunnerMessage = s;
        implementationRunner = new NoisyImplementationRunner(implementationRunnerMessage, auditStream);
    }

    @Given("recording server is returning error")
    public void recoringServerNotExposingEndpoints() throws UnirestException {
        recordingServerStub.reset();
    }

    @Given("^journeyId is \"([^\"]*)\"$")
    public void journeyid_is(String journeyId) {
        this.journeyId = journeyId;
    }

    @Given("the challenge server is broken")
    public void challengeServerIsBroken() throws UnirestException {
        challengeServerStub.reset();
    }

    @Given("^the journeyId contains special characters$")
    public void the_journeyId_is() {
        journeyId = "uvwxyz012===3456789===+/==";
    }

    // When
    @When("user starts client")
    public void userStartsChallenge() {
        ChallengeSessionConfig config = ChallengeSessionConfig.forJourneyId(journeyId)
                .withServerHostname(challengeHostname)
                .withPort(port)
                .withColours(true)
                .withAuditStream(auditStream)
                .withRecordingSystemShouldBeOn(true);

        ChallengeSession.forRunner(implementationRunner)
                .withConfig(config)
                .withActionProvider(actionProviderCallback)
                .start();
    }

    // Then

    @Then("the server interaction should contain the following lines:$")
    public void checkServerInteractionContainsLines(String expectedOutput) {
        String total = getTotalStdout();
        String[] lines = expectedOutput.split("\n");
        for (String line : lines) {
            assertThat("Expected string is not contained in output", total, containsString(line));
        }
    }

    @Then("the server interaction should look like:$")
    public void server_interaction_should_look_like(String expectedOutput) {
        String total = getTotalStdout();
        assertThat("Expected string is not contained in output", total, containsString(expectedOutput));
    }

    @And("the recording system should be notified with \"([^\"]*)\"$")
    public void recording_system_should_be_notified_with(String expectedOutput) throws UnirestException {
        recordingServerStub.verifyEndpointWasHit("/notify", "POST", expectedOutput);
    }

    @And("the recording system should have received a stop signal$")
    public void recording_system_should_receive_stop_signal() throws UnirestException {
        recordingServerStub.verifyEndpointWasHit("/stop", "POST", "");
    }

    @Then("the file \"([^\"]*)\" should contain$")
    public void checkFileContainsDescription(String file, String text) throws IOException {
        BufferedReader inputReader = new BufferedReader(new FileReader(file));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = inputReader.readLine()) != null) {
            content.append(line);
            content.append("\n");
        }
        String c = content.toString();
        assertThat("Contents of the file is not what is expected", c, equalTo(text));
    }

    @Then("the implementation runner should be run with the provided implementations")
    public void checkQueueClientRunningImplementation() {
        String total = getTotalStdout();
        assertThat(total, containsString(implementationRunnerMessage));
    }

    @Then("the client should not ask the user for input")
    public void checkClientDoesNotAskForInput() {
        String total = getTotalStdout();
        assertThat(total, not(containsString("Selected action is:")));
    }

    private String getTotalStdout() {
        return ((TestAuditStream) auditStream).getTotal();
    }
}
