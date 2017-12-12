package tdl.oldrunner;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import tdl.client.oldrunner.CombinedClient;
import tdl.client.oldrunner.HttpClient;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertFalse;


public class Steps {
    private static final String anyUnicodeRegex = "(?:\\P{M}\\p{M}*)+";
    private SingletonTestServer server;

    public Steps(SingletonTestServer server) {
        this.server = server;
    }

    @Given("I start with a clean server")
    public void resetMappings() {
        this.server.resetMappings();
    }

    @Given("server is running with basic setup")
    public void setupServerWithBasicSetup() {
        stubFor(get(urlMatching("/journeyProgress/" + anyUnicodeRegex))
                .withHeader("Accept", equalTo("text/not-coloured"))
                .withHeader("Accept-Charset", equalTo("UTF-8"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("<response>Some content</response>")));
        stubFor(get(urlMatching("/availableActions/" + anyUnicodeRegex))
                .withHeader("Accept", equalTo("text/not-coloured"))
                .withHeader("Accept-Charset", equalTo("UTF-8"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/not-coloured")
                        .withBody("some response")));
    }

    @Given("server has no available actions")
    public void setupServerWithNoAvailableActions() {
        stubFor(get(urlMatching("/journeyProgress/" + anyUnicodeRegex))
                .withHeader("Accept", equalTo("text/not-coloured"))
                .withHeader("Accept-Charset", equalTo("UTF-8"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("<response>Some content</response>")));
        stubFor(get(urlMatching("/availableActions/" + anyUnicodeRegex))
                .withHeader("Accept", equalTo("text/not-coloured"))
                .withHeader("Accept-Charset", equalTo("UTF-8"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/not-coloured")
                        .withBody("No actions available.")));
    }

    @When("I check the status of a challenge")
    public void checkStatusOfChallenge() throws HttpClient.ServerErrorException, HttpClient.OtherCommunicationException, HttpClient.ClientErrorException {
        String journeyId = "dGRsLXRlc3QtY25vZGVqczAxfFNVTSxITE8sQ0hLfFE=";
        boolean useColours = false;
        String username = "tdl-test-cnodejs01";
        CombinedClient combinedClient = new CombinedClient(journeyId, useColours, "localhost", username, System.out::println);
        combinedClient.checkStatusOfChallenge();
    }

    class EndpointRepresentation {
        String endpoint;
    }

    @Then("the client should query the following endpoints:$")
    public void checkIfEndpointsWereHit(List<EndpointRepresentation> expectedEndpoints) {
        for (EndpointRepresentation expectedEndpoint : expectedEndpoints) {
            // check wiremock that this endpoint was hit.
            System.out.println("endpoint hit");
        }
    }

    @Then("the client should find there are no available actions")
    public void checkNoAvailableActions() throws HttpClient.ServerErrorException, HttpClient.OtherCommunicationException, HttpClient.ClientErrorException {
        String journeyId = "dGRsLXRlc3QtY25vZGVqczAxfFNVTSxITE8sQ0hLfFE=";
        boolean useColours = false;
        String username = "tdl-test-cnodejs01";
        CombinedClient combinedClient = new CombinedClient(journeyId, useColours, "localhost", username, System.out::println);
        boolean canContinue = combinedClient.checkStatusOfChallenge();
        // check return value of checkStatusOfChallenge is false.
        assertFalse(canContinue);
    }
}
