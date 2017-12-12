package tdl.oldrunner;

import com.mashape.unirest.http.exceptions.UnirestException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import tdl.client.oldrunner.CombinedClient;
import tdl.client.oldrunner.HttpClient;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;


public class Steps {
    private boolean deployCallbackHit = false;

    // Given

    @Given("I start with a clean server")
    public void teardown() throws UnirestException {
        WiremockProcess.reset();
        deployCallbackHit = false;
    }

    @Given("server is running with basic setup")
    public void setupServerWithBasicSetup() throws UnirestException {
        setupServerWithMostEndpoints();
        setupServerWithBasicAvailableActions();
    }

    @Given("server has no available actions")
    public void setupServerWithNoAvailableActions() throws UnirestException {
        setupServerWithMostEndpoints();
        createEndpointNoAvailableActions();
    }

    // When

    @When("user checks the status of a challenge")
    public void checkStatusOfChallenge() throws HttpClient.ServerErrorException, HttpClient.OtherCommunicationException, HttpClient.ClientErrorException {
        CombinedClient combinedClient = setupCombinedClient();
        combinedClient.checkStatusOfChallenge();
    }

    @When("^user enters input \"([^\"]*)\"$")
    public void userEntersInputForAChallenge(String input) throws HttpClient.ServerErrorException, HttpClient.OtherCommunicationException, HttpClient.ClientErrorException {
        CombinedClient combinedClient = setupCombinedClient();
        combinedClient.executeUserAction(input, () -> deployCallbackHit = true, null);
    }

    class EndpointRepresentation {
        String endpoint;
        String methodType;
    }

    // Then

    @Then("the client should query the following endpoints:$")
    public void checkIfEndpointsWereHit(List<EndpointRepresentation> hitEndpoints) throws UnirestException {
        for (EndpointRepresentation hitEndpoint : hitEndpoints) {
            WiremockProcess.verifyEndpointWasHit(hitEndpoint.endpoint, hitEndpoint.methodType);
        }
    }

    @Then("the client should find there are no available actions")
    public void checkNoAvailableActions() throws HttpClient.ServerErrorException, HttpClient.OtherCommunicationException, HttpClient.ClientErrorException {
        CombinedClient combinedClient = setupCombinedClient();
        boolean canContinue = combinedClient.checkStatusOfChallenge();
        assertFalse("User is able to continue the journey, despite the fact they should be finished.", canContinue);
    }

    // And

    @And("the deploy callback should be hit")
    public void deployCallbackHit() {
        String failMessage = "Deploy callback was not hit";
        assertThat(failMessage, deployCallbackHit, equalTo(true));
    }

    // Helper functions

    private void setupServerWithMostEndpoints() throws UnirestException {
        WiremockProcess.createGetStubMappingForEndpointWithBody("journeyProgress", "Some content");
        WiremockProcess.createGetStubMappingForEndpointWithBody("roundDescription", "Some content");
        WiremockProcess.createPostStubMappingForEndpointWithBody("action/start", "Some content");
        WiremockProcess.createPostStubMappingForEndpointWithBody("action/deploy", "Some content");
        WiremockProcess.createPostStubMappingForEndpointWithBody("action/continue", "Some content");
        WiremockProcess.createPostStubMappingForEndpointWithBody("action/pause", "Some content");
    }

    private void setupServerWithBasicAvailableActions() throws UnirestException {
        WiremockProcess.createGetStubMappingForEndpointWithBody("availableActions", "Some content");
    }

    private void createEndpointNoAvailableActions() throws UnirestException {
        WiremockProcess.createGetStubMappingForEndpointWithBody("availableActions", "No actions available.");
    }

    private CombinedClient setupCombinedClient() {
        String journeyId = "dGRsLXRlc3QtY25vZGVqczAxfFNVTSxITE8sQ0hLfFE=";
        boolean useColours = false;
        String username = "tdl-test-cnodejs01";
        return new CombinedClient(journeyId, useColours, "localhost", username, System.out::println);
    }

}
