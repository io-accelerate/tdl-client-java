package competition.client;

import competition.client.abstractions.UserImplementation;
import cucumber.api.PendingException;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.contrib.java.lang.system.SystemOutRule;
import utils.jmx.broker.RemoteJmxQueue;
import utils.stream.LogPrintStream;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by julianghionoiu on 11/10/2015.
 */
public class MyStepdefs {
    private final SingletonTestBroker broker;

    // Test broker location
    private static final String HOSTNAME = "localhost";
    private static final int PORT = 21616;

    // Variables set by the background tasks
    RemoteJmxQueue requestQueue;
    RemoteJmxQueue responseQueue;
    Client client;

    //Testing utils
    LogPrintStream logPrintStream;

    public MyStepdefs(SingletonTestBroker broker) {
        this.broker = broker;
        this.logPrintStream = new LogPrintStream(System.out);
    }

    @Given("^I start with a clean broker$")
    public void create_the_queues() throws Throwable {
        String username = "testuser";
        requestQueue = broker.addQueue(username +".req");
        requestQueue.purge();

        responseQueue = broker.addQueue(username +".resp");
        responseQueue.purge();

        logPrintStream.clearLog();
        client = new Client(HOSTNAME, PORT, username, logPrintStream);
    }

    @Given("^I receive the following requests:$")
    public void initialize_request_queue(List<String> requests) throws Throwable {
        for (String request : requests) {
            requestQueue.sendTextMessage(request);
        }
    }

    @When("^I go live with an implementation that adds to numbers$")
    public void user_goes_live_with_correct_solution() throws Throwable {

        UserImplementation CORRECT_SOLUTION = params -> {
            Integer x = Integer.parseInt(params[0]);
            Integer y = Integer.parseInt(params[1]);
            return x + y;
        };

        client.goLiveWith(CORRECT_SOLUTION);
    }

    @Then("^the client should consume all requests$")
    public void the_client_should_consume_all_requests() throws Throwable {
        assertThat("Requests have not been consumed",requestQueue.getSize(), equalTo(asLong(0)));
    }

    @And("^the client should publish the following responses:$")
    public void the_client_should_publish_the_following_responses(List<String> expectedResponses) throws Throwable {
        assertThat("The responses are not correct",responseQueue.getMessageContents(), equalTo(expectedResponses));
    }

    @Then("^the client should display to console:$")
    public void the_client_should_display_to_console(List<String> expectedOutputs) throws Throwable {
        String output = logPrintStream.getLog();
        for (String expectedLine : expectedOutputs) {
            assertThat(output, containsString(expectedLine));
        }
    }

    //~~~ Utils

    private static Long asLong(Integer value) {
        return (long) value;
    }
}
