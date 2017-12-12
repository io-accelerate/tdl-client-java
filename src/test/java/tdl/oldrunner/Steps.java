package tdl.oldrunner;

import com.mashape.unirest.http.exceptions.UnirestException;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import tdl.client.oldrunner.CombinedClient;
import tdl.client.oldrunner.HttpClient;

import java.util.List;

import static org.junit.Assert.assertFalse;


public class Steps {
    private static final String anyUnicodeRegex = "(?:\\P{M}\\p{M}*)+";

    @Given("I start with a clean server")
    public void resetMappings() throws UnirestException {
        WiremockProcess.reset();
    }

    @Given("server is running with basic setup")
    public void setupServerWithBasicSetup() throws UnirestException {
        WiremockProcess.createGetStubMappingForEndpointWithBody("journeyProgress", "Some content");
        WiremockProcess.createGetStubMappingForEndpointWithBody("availableActions", "Some content");
    }

    @Given("server has no available actions")
    public void setupServerWithNoAvailableActions() throws UnirestException {
        WiremockProcess.createGetStubMappingForEndpointWithBody("journeyProgress", "Some content");
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
    public void checkIfEndpointsWereHit(List<EndpointRepresentation> hitEndpoints) throws UnirestException {
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

    private void mapEndpointWithNoActionsAvailable() throws UnirestException {
        WiremockProcess.createGetStubMappingForEndpointWithBody("availableActions", "No actions available.");
    }

    private void verifyEndpointWasHit(String endpoint) throws UnirestException {
        switch (endpoint) {
            case "journeyProgress":
            case "availableActions":
            case "roundDescription":
                WiremockProcess.verifyGetEndpointWasHit(endpoint);
                break;
            case "start":
            case "deploy":
            case "pause":
            case "continue":
                WiremockProcess.verifyPostEndpointWasHit(endpoint);
                break;
            default:
                // fail
                throw new RuntimeException("None of the requests matched");
        }
    }
}
