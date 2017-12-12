package tdl.oldrunner;

import com.github.tomakehurst.wiremock.matching.UrlPattern;
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
        createGetMappingForEndpointWithBody("journeyProgress", "Some content");
        createGetMappingForEndpointWithBody("availableActions", "Some content");
    }

    @Given("server has no available actions")
    public void setupServerWithNoAvailableActions() {
        createGetMappingForEndpointWithBody("journeyProgress", "Some content");
        mapEndpointWithNoActionsAvailable();
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
    public void checkIfEndpointsWereHit(List<EndpointRepresentation> hitEndpoints) {
        for (EndpointRepresentation hitEndpoint : hitEndpoints) {
            verifyEndpointWasHit(hitEndpoint.endpoint);
        }
    }

    @Then("the client should find there are no available actions")
    public void checkNoAvailableActions() throws HttpClient.ServerErrorException, HttpClient.OtherCommunicationException, HttpClient.ClientErrorException {
        String journeyId = "dGRsLXRlc3QtY25vZGVqczAxfFNVTSxITE8sQ0hLfFE=";
        boolean useColours = false;
        String username = "tdl-test-cnodejs01";
        CombinedClient combinedClient = new CombinedClient(journeyId, useColours, "localhost", username, System.out::println);
        boolean canContinue = combinedClient.checkStatusOfChallenge();
        assertFalse("User is able to continue the journey, despite the fact they should be finished.", canContinue);
    }


    // Helper functions

    private void mapEndpointWithNoActionsAvailable() {
        createGetMappingForEndpointWithBody("availableActions", "No actions available.");
    }
    private void createGetMappingForEndpointWithBody(String endpoint, String body) {
        stubFor(get(urlMatchingEndpoint(endpoint))
                .withHeader("Accept", equalTo("text/not-coloured"))
                .withHeader("Accept-Charset", equalTo("UTF-8"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(body)));
    }

    private void verifyEndpointWasHit(String endpoint) {
        switch (endpoint) {
            case "journeyProgress":
            case "availableActions":
            case "roundDescription":
                verify(getRequestedFor(urlMatchingEndpoint(endpoint)));
                break;
            case "start":
            case "deploy":
            case "pause":
            case "continue":
                verify(postRequestedFor(urlMatchingEndpoint(endpoint)));
                break;
            default:
                // fail
                throw new RuntimeException("None of the requests matched");
        }
    }

    private UrlPattern urlMatchingEndpoint(String endpoint) {
        return urlMatching("/" + endpoint + "/" + anyUnicodeRegex);
    }
}
