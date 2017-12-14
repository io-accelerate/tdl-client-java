package tdl.runner;

import com.mashape.unirest.http.exceptions.UnirestException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import tdl.client.abstractions.Request;
import tdl.client.abstractions.UserImplementation;
import tdl.client.abstractions.response.Response;
import tdl.client.actions.ClientAction;
import tdl.client.runner.ClientRunner;
import tdl.client.runner.HttpClient;
import tdl.client.transport.BrokerCommunicationException;
import tdl.client.transport.RemoteBroker;

import java.io.*;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class Steps {
    private boolean deployCallbackHit = false;
    private WiremockProcess wiremockProcess;
    private String hostname;
    private int port;
    private boolean sumHit = false;
    private boolean helloHit = false;
    private boolean fizzBuzzHit = false;
    private boolean checkoutHit = false;
    private PrintStream printStream;
    private BufferedReader bufferedReader;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private Thread clientRunnerThread;
    private String recordingSystemCallbackText;
    private String[] userCommandLineArgs;

    // Given

    @Given("There is a challenge server running on \"([^\"]*)\" port (\\d+)$")
    public void setupServerWithSetup(String hostname, int port) throws UnirestException {
        this.hostname = hostname;
        this.port = port;
        wiremockProcess = new WiremockProcess(hostname, port);
        wiremockProcess.reset();
    }

    class ServerConfig {
        String verb;
        String endpoint;
        int returnStatus;
        String returnBody;
    }

    @And("It exposes the following endpoints$")
    public void configureEndpoint(List<ServerConfig> configs) throws UnirestException {
        for (ServerConfig config: configs) {
            wiremockProcess.createStubMapping(config.verb, config.endpoint, config.returnStatus, config.returnBody);
        }
    }

    @And("expects requests to have the Accept header set to \"([^\"]*)\"")
    public void configureAcceptHeader(String header) throws UnirestException {
        wiremockProcess.addHeaderToStubs(header);
    }

    @Given("server endpoint \"([^\"]*)\" returns \"([^\"]*)\"")
    public void setupServerEndpointResponse(String endpoint, String returnValue) {
        wiremockProcess.adjustStubMappingResponse(endpoint, returnValue);
    }

    // When

    @When("user starts client")
    public void userStartsChallenge() throws UnirestException {
        printStream = new PrintStream(outContent);
        wiremockProcess.configureServer();
        String journeyId = "dGRsLXRlc3QtY25vZGVqczAxfFNVTSxITE8sQ0hLfFE=";
        String username = "tdl-test-cnodejs01";
        userCommandLineArgs = new String[]{};

        ClientAction clientAction = new ClientAction() {
            @Override
            public void afterResponse(RemoteBroker remoteBroker, Request request, Response response) throws BrokerCommunicationException {

            }

            @Override
            public Optional<Request> getNextRequest(RemoteBroker t) throws BrokerCommunicationException {
                return Optional.empty();
            }

            @Override
            public String getAuditText() {
                return null;
            }
        };
        UserImplementation userImplementation = new UserImplementation() {
            @Override
            public Object process(String... params) {
                return null;
            }
        };
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
        Consumer<String> notifyRecordSystemCallback = new Consumer<String>() {
            @Override
            public void accept(String s) {
                recordingSystemCallbackText = s;
            }
        };

        // start in a background thread, check if thread finished for exit.

        ClientRunner clientRunner = ClientRunner.forUsername(username)
                .withServerHostname(hostname)
                .withPort(port)
                .withJourneyId(journeyId)
                .withColours(true)
                .withDeployCallback(() -> deployCallbackHit = true)
                .withDeployAction(clientAction)
                .withRecordingSystemOk(true)
                .withBufferedReader(bufferedReader)
                .withOutputStream(printStream)
                .withNotifyRecordSystemCallback(notifyRecordSystemCallback)
                .withSolutionFor("sum", p -> sum)
                .withSolutionFor("hello", p -> hello)
                .withSolutionFor("fizz_buzz", p -> fizzBuzz)
                .withSolutionFor("checkout", p -> checkout);

        clientRunnerThread = new Thread(() -> clientRunner.start(userCommandLineArgs));
        clientRunnerThread.start();
    }

    @When("user types action \"([^\"]*)\"$")
    @And("types action \"([^\"]*)\"$")
    public void userEntersInput(String input) throws HttpClient.ServerErrorException, HttpClient.OtherCommunicationException, HttpClient.ClientErrorException {
        bufferedReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(input.getBytes())));
    }

    // Then

    @Then("the client should ask the user for input and wait")
    public void checkUserWaitsForInput() {
        assertTrue("Thread exited, client must have stopped", clientRunnerThread.isAlive());
        // TODO check output?
    }

    @Then("^the user should see:$")
    public void parseInput(String expectedOutput) {
        assertEquals(expectedOutput, outContent.toString());
    }

    @And("the client should exit")
    public void exitClient() {
        assertFalse("Client is still running", clientRunnerThread.isAlive());
    }

    @Then("the file \"([^\"]*)\" should contain \"([^\"]*)\"$")
    public void checkFileContainsDescription(String file, String text) throws IOException {
        BufferedReader inputReader = new BufferedReader(new FileReader(file));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = inputReader.readLine()) != null){
            content.append(line);
        }
        String c = content.toString();
        assertThat("contents of the file is not what is expected", text, equalTo(c));
    }

    @And("the recording system should be notified with \"([^\"]*)\"$")
    public void checkRecordingSystemNotified(String text) {
        assertThat("Recording system not notified", text, equalTo(recordingSystemCallbackText));
    }

    @Then("the queue client should be run with the provided implementations")
    public void checkQueueClientRunningImplementation() {
        // how to detect the provided implementation?
        assertTrue("Checkout implementation wasn't hit", checkoutHit);
        assertTrue("FizzBuzz implementation wasn't hit", fizzBuzzHit);
        assertTrue("Hello implementation wasn't hit", helloHit);
        assertTrue("Sum implementation wasn't hit", sumHit);
    }

    @Then("the client should not ask the user for input")
    public void checkClientDoesNotAskForInput() {
        assertFalse("Client is still running", clientRunnerThread.isAlive());
        // TODO check output?
    }

}
