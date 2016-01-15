package tdl.client;

import tdl.client.abstractions.ProcessingRules;
import cucumber.api.java.en.*;
import tdl.client.abstractions.UserImplementation;
import tdl.client.actions.ClientAction;
import tdl.client.actions.PublishAction;
import tdl.client.actions.PublishAndStopAction;
import tdl.client.actions.StopAction;
import tdl.client.audit.StdoutAuditStream;
import utils.jmx.broker.RemoteJmxQueue;
import utils.logging.LogAuditStream;

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
    LogAuditStream logAuditStream;

    public ClientSteps(SingletonTestBroker broker) {
        this.broker = broker;
        this.logAuditStream = new LogAuditStream(new StdoutAuditStream());
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

        logAuditStream.clearLog();
        client = new Client(HOSTNAME, PORT, username, logAuditStream);
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

    private static final Map<String, UserImplementation> USER_IMPLEMENTATIONS = new HashMap<String, UserImplementation >() {{
        put("add two numbers", params -> {
            Integer x = Integer.parseInt(params[0]);
            Integer y = Integer.parseInt(params[1]);
            return x + y;
        });
        put("increment number", params -> {
            Integer x = Integer.parseInt(params[0]);
            return x + 1;
        });
        put("return null", params -> null);
        put("throw exception", param -> {
            throw new IllegalStateException("faulty user code");
        });
        put("some logic", params -> "ok");
    }};


    private static UserImplementation asImplementation(String call) {
        if (USER_IMPLEMENTATIONS.containsKey(call)) {
            return USER_IMPLEMENTATIONS.get(call);
        } else {
            throw new IllegalArgumentException("Not a valid implementation reference: \"" + call+"\"");
        }
    }

    private static final Map<String, ClientAction> CLIENT_ACTIONS = new HashMap<String, ClientAction >() {{
        put("publish", new PublishAction());
        put("stop", new StopAction());
        put("publish and stop", new PublishAndStopAction());
    }};

    private static ClientAction asAction(String actionName) {
        if (CLIENT_ACTIONS.containsKey(actionName)) {
            return CLIENT_ACTIONS.get(actionName);
        } else {
            throw new IllegalArgumentException("Not a valid action reference: \"" + actionName+"\"");
        }
    }

    public class ProcessingRuleRepresentation {
        String method;
        String call;
        String action;
    }

    @When("^I go live with the following processing rules:$")
    public void go_live(List<ProcessingRuleRepresentation> listOfRules) throws Throwable {
        ProcessingRules processingRules = new ProcessingRules();
        listOfRules.forEach((ruleLine) ->
                        processingRules.add(ruleLine.method, asImplementation(ruleLine.call), asAction(ruleLine.action))
        );

        client.goLiveWith(processingRules);
    }

    //~~~~~ Assertions

    @Then("^the client should consume all requests$")
    public void request_queue_empty() throws Throwable {
        assertThat("Requests have not been consumed",requestQueue.getSize(), equalTo(asLong(0)));
    }

    @Then("^the client should consume first request$")
    public void request_queue_less_than_one() throws Throwable {
        assertThat("Wrong number of requests have been consumed",requestQueue.getSize(), equalTo(asLong(initialRequestCount-1)));
    }

    @And("^the client should publish the following responses:$")
    public void response_queue_contains_expected(List<String> expectedResponses) throws Throwable {
        assertThat("The responses are not correct",responseQueue.getMessageContents(), equalTo(expectedResponses));
    }

    @Then("^the client should display to console:$")
    public void the_client_should_display_to_console(List<String> expectedOutputs) throws Throwable {
        String output = logAuditStream.getLog();
        for (String expectedLine : expectedOutputs) {
            assertThat(output, containsString(expectedLine));
        }
//        System.out.println(output);
    }

    @But("^the client should not display to console:$")
    public void the_client_should_not_display_to_console(List<String> rejectedOutputs) throws Throwable {
        String output = logAuditStream.getLog();
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
