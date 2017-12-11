package tdl.oldrunner;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import tdl.client.ClientSteps;
import tdl.client.oldrunner.CombinedClient;
import tdl.client.oldrunner.HttpClient;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class Steps {
    //~~ Old runner
    private WireMockServer wireMockServer;

    public Steps() {
        wireMockServer = new WireMockServer(wireMockConfig().port(8089)); //No-args constructor will start on port 8080, no HTTPS
    }

    @Given("server is running with basic setup")
    public void setupServerWithBasicSetup() {
        stubFor(get(urlMatching("/journeyProgress/[a-z0-9]+"))
                .withHeader("Accept", equalTo("text/plain"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("<response>Some content</response>")));
        stubFor(get(urlMatching("/availableActions/[a-z0-9]+"))
                .withHeader("Accept", equalTo("text/plain"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("some response")));
        wireMockServer.start();
    }

    @Given("server has no available actions")
    public void setupServerWithNoAvailableActions() {
        stubFor(get(urlMatching("/journeyProgress/[a-z0-9]+"))
                .withHeader("Accept", equalTo("text/plain"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("<response>Some content</response>")));
        stubFor(get(urlMatching("/availableActions/[a-z0-9]+"))
                .withHeader("Accept", equalTo("text/plain"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("No available actions")));
        wireMockServer.start();
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
