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

import java.io.PrintStream;
import java.util.Optional;
import java.util.function.Consumer;


public class Steps {
    private boolean deployCallbackHit = false;
    private WiremockProcess wiremockProcess;
    private String hostname;
    private int port;
    private boolean sumHit = false;
    private boolean helloHit = false;
    private boolean fizzBuzzHit = false;
    private boolean checkoutHit = false;
    private PrintStream printStream = System.out;

    // Given

    @Given("There is a challenge server running on \"([^\"]*)\" port \\d+$")
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
    public void configureEndpoint(ServerConfig config) throws UnirestException {
        wiremockProcess.createStubMapping(config.verb, config.endpoint, config.returnStatus, config.returnBody);
    }

    @And("And expects requests to have the Accept header set to \"text/coloured\"")
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
        wiremockProcess.configureServer();
        String journeyId = "dGRsLXRlc3QtY25vZGVqczAxfFNVTSxITE8sQ0hLfFE=";
        String username = "tdl-test-cnodejs01";
        String[] args = new String[]{};
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
        Consumer<String> saveDescriptionRoundManagement = new Consumer<String>() {
            @Override
            public void accept(String s) {
                // do nothing
            }
        };
        Consumer<String> printer = new Consumer<String>() {
            @Override
            public void accept(String s) {
                // TODO read rather than print
                printStream.println(s);
            }
        };

        ClientRunner.forUsername(username)
                .withServerHostname(hostname)
                .withPort(port)
                .withJourneyId(journeyId)
                .withColours(true)
                .withDeployCallback(() -> deployCallbackHit = true)
                .withUserInput(Steps::getUserInput)
                .withDeployAction(clientAction)
                .withPrinter(printer)
                .withSaveDescriptionImplementation(userImplementation)
                .withRecordingSystemOk(true)
                .withSaveDescriptionRoundManagement(saveDescriptionRoundManagement)
                .withSolutionFor("sum", p -> sum)
                .withSolutionFor("hello", p -> hello)
                .withSolutionFor("fizz_buzz", p -> fizzBuzz)
                .withSolutionFor("checkout", p -> checkout)
                .start(args);
    }

    public static String getUserInput(String[] args) {
        return args.length > 0 ? args[0] : readInputFromConsole();
    }

    private static String readInputFromConsole() {
        // TODO instead of system.in, use a different stream so we can enter input retrospectively
        throw new RuntimeException("not implemented!");
    }

    @When("^user types action \"([^\"]*)\"$")
    public void userEntersInputForAChallenge(String input) throws HttpClient.ServerErrorException, HttpClient.OtherCommunicationException, HttpClient.ClientErrorException {
        // TODO: enter input
        throw new RuntimeException("not implemented!");
    }

    // Then

    @Then("the user should see:\"\"\"([^\"]*)\"\"\"$")
    public void parseInput(String input) {
        System.out.println("input = " + input);
        // TODO: keep track of input printed by the console
        throw new RuntimeException("not implemented");
    }

    @Then("Then the client should ask the user for input and wait")
    public void checkPromptWaitForInput() {
        throw new RuntimeException("not implemented");
    }

    @And("the client should exit")
    public void exitClient() {
        throw new RuntimeException("not implemented");
    }

    @Then("the file \"([^\"]*)\" should contain \"([^\"]*)\"$")
    public void checkFileContainsDescription(String file, String text) {
        throw new RuntimeException("not implemented");
    }

    @And("the recording system should be notified with \"([^\"]*)\"$")
    public void checkRecordingSystemNotified(String text) {
        throw new RuntimeException("not implemented");
    }

    @Then("the queue client should be run with the provided implementations")
    public void checkQueueClientRunningImplementation() {
        // unsure about this
        throw new RuntimeException("not implemented");
    }

    @Then("the client should not ask the user for input")
    public void checkClientDoesNotAskForInput() {
        throw new RuntimeException("not implemented");
    }

}
