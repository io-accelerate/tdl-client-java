package tdl.client.runner;

import com.mashape.unirest.http.exceptions.UnirestException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import tdl.client.queue.ImplementationRunner;
import tdl.client.queue.NoisyImplementationRunner;
import tdl.client.queue.QuietImplementationRunner;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsEqual.equalTo;


public class RunnerSteps {
    private WiremockProcess challengeServerStub;
    private WiremockProcess recordingServerStub;
    private String challengeHostname;
    private String recordingHostname;
    private int port;
    private final IConsoleOut consoleOut = new TestConsoleOut();
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
        this.recordingHostname = hostname;
        recordingServerStub = new WiremockProcess(hostname, port);
        recordingServerStub.reset();
    }

    class ServerConfig {
        String verb;
        String endpointEquals;
        String endpointMatches;
        int status;
        String responseBody;
        String acceptHeader;
        String statusMessage;
    }

    @And("the challenge server exposes the following endpoints$")
    public void configureChallengeServerEndpoint(List<ServerConfig> configs) throws UnirestException {
        for (ServerConfig config: configs) {
            challengeServerStub.createNewMapping(config);
        }
    }

    @And("the recording server exposes the following endpoints$")
    public void configureRecordingServerEndpoint(List<ServerConfig> configs) throws UnirestException {
        for (ServerConfig config: configs) {
            recordingServerStub.createNewMapping(config);
        }
    }

    @Given("^the challenge server returns (\\d+) for all requests$")
    public void the_challenge_server_returns_for_all_requests(int returnCode) throws Throwable {
        ServerConfig config = new ServerConfig();
        config.endpointMatches = "^(.*)";
        config.status = returnCode;
        config.verb = "ANY";
        challengeServerStub.createNewMapping(config);
    }

    @Given("^the challenge server returns (\\d+), response body \"([^\"]*)\" for all requests$")
    public void the_challenge_server_returns_response_body_for_all_requests(int returnCode, String body) throws Throwable {
        ServerConfig config = new ServerConfig();
        config.endpointMatches = "^(.*)";
        config.status = returnCode;
        config.verb = "ANY";
        config.responseBody = body;
        challengeServerStub.createNewMapping(config);
    }

    @And("the challenges folder is empty")
    public void deleteContentsOfChallengesFolder() throws IOException {
        Path path =  Paths.get("challenges");
        deleteFolderContents(path.toFile());
    }

    void deleteFolderContents(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolderContents(f);
                } else {
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
        implementationRunner = new NoisyImplementationRunner(implementationRunnerMessage, consoleOut);
    }

    @Given("recording server is returning error")
    public void recoringServerNotExposingEndpoints() throws UnirestException {
        recordingServerStub.reset();
    }

    @Given("^journeyId is \"([^\"]*)\"$")
    public void journeyid_is(String journeyId) throws Throwable {
        this.journeyId = journeyId;
    }

    @Given("the challenge server is broken")
    public void challengeServerIsBroken() throws UnirestException {
        challengeServerStub.reset();
    }

    @Given("^the journeyId contains special characters$")
    public void the_journeyId_is() throws Throwable {
        journeyId = "uvwxyz012===3456789===+/==";
    }

    // When
    @When("user starts client")
    public void userStartsChallenge() throws UnirestException {
        String username = "tdl-test-cnodejs01";

        ChallengeSession session = ChallengeSession.forUsername(username)
                .withServerHostname(challengeHostname)
                .withPort(port)
                .withJourneyId(journeyId)
                .withColours(true)
                .withConsoleOut(consoleOut)
                .withRecordingSystemOn(true)
                .withImplementationRunner(implementationRunner)
                .withActionProvider(actionProviderCallback);

        session.start();
    }

    // Then

    @Then("the server interaction should contain the following lines:$")
    public void checkServerInteractionContainsLines(String expectedOutput) throws IOException, InteractionException {
        String total = ((TestConsoleOut)consoleOut).getTotal();
        String[] lines = expectedOutput.split("\n");
        for (String line : lines) {
            assertThat("Expected string is not contained in output", total, containsString(line));
        }
    }

    @Then("the server interaction should look like:$")
    public void server_interaction_should_look_like(String expectedOutput) throws IOException, InteractionException {
        String total = ((TestConsoleOut)consoleOut).getTotal();
        assertThat("Expected string is not contained in output", total, containsString(expectedOutput));
    }

    @And("the recording system should be notified with \"([^\"]*)\"$")
    public void recording_system_should_be_notified_with(String expectedOutput) throws IOException, InteractionException, UnirestException {
        recordingServerStub.verifyEndpointWasHit("/notify", "POST", expectedOutput);
    }

    @Then("the file \"([^\"]*)\" should contain$")
    public void checkFileContainsDescription(String file, String text) throws IOException, InteractionException {
        BufferedReader inputReader = new BufferedReader(new FileReader(file));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = inputReader.readLine()) != null){
            content.append(line);
            content.append("\n");
        }
        String c = content.toString();
        assertThat("Contents of the file is not what is expected", c, equalTo(text));
    }

    @Then("the implementation runner should be run with the provided implementations")
    public void checkQueueClientRunningImplementation() throws InteractionException {
        String total = ((TestConsoleOut)consoleOut).getTotal();
        assertThat(total, containsString(implementationRunnerMessage));
    }

    @Then("the client should not ask the user for input")
    public void checkClientDoesNotAskForInput() throws InteractionException {
        String total = ((TestConsoleOut)consoleOut).getTotal();
        assertThat(total, not(containsString("Selected action is:")));
    }
}
