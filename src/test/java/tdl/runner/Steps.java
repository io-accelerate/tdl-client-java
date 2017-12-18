package tdl.runner;

import com.mashape.unirest.http.exceptions.UnirestException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import tdl.client.abstractions.UserImplementation;
import tdl.client.runner.ChallengeSession;
import tdl.client.runner.IConsoleOut;
import tdl.client.runner.ImplementationRunner;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertTrue;


public class Steps {
    private WiremockProcess challengeServerStub;
    private WiremockProcess recordingServerStub;
    private String hostname;
    private int port;
    private boolean sumHit = false;
    private boolean helloHit = false;
    private boolean fizzBuzzHit = false;
    private boolean checkoutHit = false;
    private String[] userCommandLineArgs;
    private BufferedReader reader;
    private PrintStream writer;
    private IConsoleOut consoleOut;

    // Given

    @Given("There is a challenge server running on \"([^\"]*)\" port (\\d+)$")
    public void setupServerWithSetup(String hostname, int port) throws UnirestException {
        this.hostname = hostname;
        this.port = port;
        challengeServerStub = new WiremockProcess(hostname, port);
        challengeServerStub.reset();
    }

    @And("There is a recording server running on \"([^\"]*)\" port (\\d+)$")
    public void setupRecordingServerWithSetup(String hostname, int port) throws UnirestException {
        recordingServerStub = new WiremockProcess(hostname, port);
        recordingServerStub.reset();
    }

    class ServerConfig {
        String verb;
        String endpoint;
        int returnStatus;
        String returnBody;
    }

    @And("the challenge server exposes the following endpoints$")
    public void configureChallengeServerEndpoint(List<ServerConfig> configs) {
        for (ServerConfig config: configs) {
            challengeServerStub.createStubMapping(config.verb, config.endpoint, config.returnStatus, config.returnBody);
        }
    }

    @And("the recording server exposes the following endpoints$")
    public void configureRecordingServerEndpoint(List<ServerConfig> configs) {
        for (ServerConfig config: configs) {
            recordingServerStub.createStubMapping(config.verb, config.endpoint, config.returnStatus, config.returnBody);
        }
    }

    @And("the challenge server expects requests to have the Accept header set to \"([^\"]*)\"")
    public void configureAcceptHeader(String header) throws UnirestException {
        challengeServerStub.addHeaderToStubs(header);
    }

    @Given("server endpoint \"([^\"]*)\" returns \"([^\"]*)\"")
    public void setupServerEndpointResponse(String endpoint, String returnValue) {
        challengeServerStub.adjustStubMappingResponse(endpoint, returnValue);
    }

    @And("the challenges folder is empty")
    public void deleteContentsOfChallengesFolder() throws IOException {
        Path path =  Paths.get("challenges");
        deleteFolderContents(path.toFile());
        new File("challenges/XR.txt").createNewFile();
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
        userCommandLineArgs = new String[]{s};
    }


    // When
    @When("user starts client")
    public void userStartsChallenge() throws UnirestException {
        challengeServerStub.configureServer();
        String journeyId = "dGRsLXRlc3QtY25vZGVqczAxfFNVTSxITE8sQ0hLfFE=";
        String username = "tdl-test-cnodejs01";

        UserImplementation sum = new UserImplementation() {
            @Override
            public Object process(String... params) {
                sumHit = true;
                return null;
            }
        };
        UserImplementation hello = new UserImplementation() {
            @Override
            public Object process(String... params) {
                helloHit = true;
                return null;
            }
        };
        UserImplementation fizzBuzz = new UserImplementation() {
            @Override
            public Object process(String... params) {
                fizzBuzzHit = true;
                return null;
            }
        };
        UserImplementation checkout = new UserImplementation() {
            @Override
            public Object process(String... params) {
                checkoutHit = true;
                return null;
            }
        };
        consoleOut = new TestConsoleOut();
        ImplementationRunner implementationRunner = ImplementationRunner.forUsername(username)
                .withHostname(hostname)
                .withConsoleOut(consoleOut)
                .withSolutionFor("sum", sum)
                .withSolutionFor("hello", hello)
                .withSolutionFor("fizz_buzz", fizzBuzz)
                .withSolutionFor("checkout", checkout);

        writer = new PrintStream(new BufferedOutputStream(System.out));
        reader = new BufferedReader(new InputStreamReader(System.in));
        ChallengeSession session = ChallengeSession.forUsername(username)
                .withServerHostname(hostname)
                .withPort(port)
                .withJourneyId(journeyId)
                .withColours(true)
                .withBufferedReader(reader)
                .withConsoleOut(consoleOut)
                .withImplementationRunner(implementationRunner);

        session.start(userCommandLineArgs);
    }

    // Then

    @Then("the server interaction should look like:$")
    public void parseInput(String expectedOutput) throws IOException, InteractionException {
        // compare to
        String total = ((TestConsoleOut)consoleOut).getTotal();
        assertThat(expectedOutput, equalTo(total));
    }

    @And("the recording system should be notified with \"([^\"]*)\"$")
    public void parseInput2(String expectedOutput) throws IOException, InteractionException {
        throw new RuntimeException("not implemented");

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
        // how to detect the provided implementation?
        assertTrue("Checkout implementation wasn't hit", checkoutHit);
        assertTrue("FizzBuzz implementation wasn't hit", fizzBuzzHit);
        assertTrue("Hello implementation wasn't hit", helloHit);
        assertTrue("Sum implementation wasn't hit", sumHit);
    }

    @Then("the client should not ask the user for input")
    public void checkClientDoesNotAskForInput() throws InteractionException {
        throw new RuntimeException("not implemented!");
    }

}
