package competition.client;

import competition.client.abstractions.UserImplementation;
import cucumber.api.PendingException;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.*;
import org.junit.contrib.java.lang.system.SystemOutRule;
import utils.jmx.broker.RemoteJmxQueue;
import utils.stream.LogPrintStream;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
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
    int initialRequestCount;
    LogPrintStream logPrintStream;

    public MyStepdefs(SingletonTestBroker broker) {
        this.broker = broker;
        this.logPrintStream = new LogPrintStream(System.out);
        this.initialRequestCount = 0;
    }

    //~~~~~ Setup

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

    @Given("^the broker is not available$")
    public void the_broker_is_not_available() throws Throwable {
        client = new Client(HOSTNAME+"1", PORT, "broker");
    }

    @Given("^I receive the following requests:$")
    public void initialize_request_queue(List<String> requests) throws Throwable {
        for (String request : requests) {
            requestQueue.sendTextMessage(request);
        }
        initialRequestCount = requests.size();
    }


    //~~~~~ Implementations

    @When("^I go live with an implementation$")
    public void I_go_live_with_an_implementation() throws Throwable {
        UserImplementation returnNull = params -> null;

        client.goLiveWith(returnNull);
    }

    @When("^I go live with an implementation that adds to numbers$")
    public void user_goes_live_with_correct_solution() throws Throwable {

        UserImplementation addNumbers = params -> {
            Integer x = Integer.parseInt(params[0]);
            Integer y = Integer.parseInt(params[1]);
            return x + y;
        };

        client.goLiveWith(addNumbers);
    }

    @When("^I go live with an implementation that returns null$")
    public void I_go_live_with_an_implementation_that_returns_null() throws Throwable {
        UserImplementation returnNull = params -> null;

        client.goLiveWith(returnNull);
    }

    @When("^I go live with an implementation that throws exception$")
    public void I_go_live_with_an_implementation_that_throws_exception() throws Throwable {
        UserImplementation throwException = param -> {
            throw new IllegalStateException("faulty user code");
        };

        client.goLiveWith(throwException);
    }

    @When("^I do a trial run with an implementation that adds to numbers$")
    public void I_do_a_trial_run_with_an_implementation_that_adds_to_numbers() throws Throwable {
        UserImplementation addNumbers = params -> {
            Integer x = Integer.parseInt(params[0]);
            Integer y = Integer.parseInt(params[1]);
            return x + y;
        };

        client.trialRunWith(addNumbers);
    }

    //~~~~~ Assertions

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

    @But("^the client should not display to console:$")
    public void the_client_should_not_display_to_console(List<String> rejectedOutputs) throws Throwable {
        String output = logPrintStream.getLog();
        for (String expectedLine : rejectedOutputs) {
            assertThat(output, not(containsString(expectedLine)));
        }
    }

    @Then("^the client should not consume any request$")
    public void the_client_should_not_consume_the_request() throws Throwable {
        assertThat("The request queue has different size. The message has been consumed.",
                requestQueue.getSize(), equalTo(asLong(initialRequestCount)));
    }

    @And("^the client should not publish any response$")
    public void the_client_should_not_publish_any_response() throws Throwable {
        assertThat("The response queue has different size. Messages have been published.",
                responseQueue.getSize(), equalTo(asLong(0)));
    }

    @Then("^I should ge no exception$")
    public void I_should_ge_no_exception() throws Throwable {
        //No exception
    }

    //~~~ Utils

    private static Long asLong(Integer value) {
        return (long) value;
    }
}
