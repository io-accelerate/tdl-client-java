package tdl.client;

import cucumber.api.PendingException;
import tdl.client.abstractions.ImplementationMap;
import tdl.client.abstractions.UserImplementation;
import cucumber.api.Transform;
import cucumber.api.Transformer;
import cucumber.api.java.en.*;
import utils.jmx.broker.RemoteJmxQueue;
import utils.stream.LogPrintStream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

/**
 * Created by julianghionoiu on 11/10/2015.
 */
public class ClientSteps {
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

    public ClientSteps(SingletonTestBroker broker) {
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
    public void client_with_wrong_broker() throws Throwable {
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

    private static final Map<String, UserImplementation> TEST_IMPLEMENTATIONS = new HashMap<>();

    static {
        TEST_IMPLEMENTATIONS.put("adds two numbers", params -> {
            Integer x = Integer.parseInt(params[0]);
            Integer y = Integer.parseInt(params[1]);
            return x + y;
        });
        TEST_IMPLEMENTATIONS.put("increment number", params -> {
            Integer x = Integer.parseInt(params[0]);
            return x + 1;
        });
        TEST_IMPLEMENTATIONS.put("returns null", params -> null);
        TEST_IMPLEMENTATIONS.put("throws exception", param -> {
            throw new IllegalStateException("faulty user code");
        });
        TEST_IMPLEMENTATIONS.put("some logic", params -> "ok");
    }

    private static UserImplementation asImplementation(String logic) {
        if (TEST_IMPLEMENTATIONS.containsKey(logic)) {
            return TEST_IMPLEMENTATIONS.get(logic);
        } else {
            throw new IllegalArgumentException("Not a valid implementation reference: \"" + logic+"\"");
        }
    }

    private static ImplementationMap asImplementationMap(Map<String, String> methodLogic) {
        ImplementationMap implementationMap = new ImplementationMap();
        methodLogic.forEach((method, logicDescription) ->
                        implementationMap.register(method, asImplementation(logicDescription))
        );
        return implementationMap;
    }

    @When("^I go live with the following implementations:$")
    public void go_live(Map<String, String> methodLogic) throws Throwable {
        client.goLiveWith(asImplementationMap(methodLogic));
    }

    @When("^I do a trial run with the following implementations:$")
    public void trial_run(Map<String, String> methodNames) throws Throwable {
        client.trialRunWith(asImplementationMap(methodNames));
    }

    //~~~~~ Assertions

    @Then("^the client should consume all requests$")
    public void request_queue_empty() throws Throwable {
        assertThat("Requests have not been consumed",requestQueue.getSize(), equalTo(asLong(0)));
    }

    @And("^the client should publish the following responses:$")
    public void response_queue_contains_expected(List<String> expectedResponses) throws Throwable {
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
    public void request_queue_unchanged() throws Throwable {
        assertThat("The request queue has different size. The message has been consumed.",
                requestQueue.getSize(), equalTo(asLong(initialRequestCount)));
    }

    @And("^the client should not publish any response$")
    public void response_queue_unchanged() throws Throwable {
        assertThat("The response queue has different size. Messages have been published.",
                responseQueue.getSize(), equalTo(asLong(0)));
    }

    @Then("^I should get no exception$")
    public void I_should_get_no_exception() throws Throwable {
        //No exception
    }

    //~~~ Utils

    private static Long asLong(Integer value) {
        return (long) value;
    }
}
