package acceptance.queue;

import com.amazonaws.services.sqs.model.CreateQueueRequest;
import cucumber.api.java.en.And;
import cucumber.api.java.en.But;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import tdl.client.audit.StdoutAuditStream;
import tdl.client.queue.ImplementationRunnerConfig;
import tdl.client.queue.QueueBasedImplementationRunner;
import tdl.client.queue.abstractions.UserImplementation;
import tdl.client.queue.actions.ClientAction;
import tdl.client.queue.actions.ClientActions;
import tdl.client.runner.connector.EventProcessingException;
import tdl.client.runner.connector.QueueEventHandlers;
import tdl.client.runner.events.ExecuteCommandEvent;
import utils.logging.LogAuditStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class QueueSteps {

    // Test broker location
    private static final String HOSTNAME = "localhost";
    private static final int PORT = 28161;

    // Variables set by the background tasks
    private CreateQueueRequest requestQueue;
    private CreateQueueRequest responseQueue;
    private QueueBasedImplementationRunner queueBasedImplementationRunner;
    private QueueBasedImplementationRunner.Builder queueBasedImplementationRunnerBuilder;

    //Testing utils
    private int initialRequestCount;
    private long processingTimeMillis;
    private final LogAuditStream logAuditStream;
    private QueueEventHandlers queueEventHandlers;
    private List<Object> capturedEvents = new ArrayList<>();
    private String requestQueueUrl;
    private String responseQueueUrl;

    public QueueSteps() {
        this.logAuditStream = new LogAuditStream(new StdoutAuditStream());
        this.initialRequestCount = 0;
        this.processingTimeMillis = 0;

        queueEventHandlers = new QueueEventHandlers();

        try {
            queueEventHandlers.put(ExecuteCommandEvent.class, capturedEvents::add);
        } catch (EventProcessingException e) {
            throw new IllegalArgumentException("Could not create queue handlers");
        }
    }

    //~~~~~ Setup

    @Given("^I start with a clean broker and a client for user \"([^\"]*)\"$")
    public void create_the_queues(String username) throws Throwable {
        logAuditStream.clearLog();
        ImplementationRunnerConfig config = new ImplementationRunnerConfig().setHostname(HOSTNAME)
                .setPort(PORT)
                .setUniqueId(username)
                .setAuditStream(logAuditStream);

        queueBasedImplementationRunnerBuilder = new QueueBasedImplementationRunner.Builder()
                .setConfig(config);

        queueBasedImplementationRunner = queueBasedImplementationRunnerBuilder.create();

        requestQueue = queueBasedImplementationRunner.getRequestQueue();
        requestQueueUrl = queueBasedImplementationRunner.getRequestQueueUrl();
        queueBasedImplementationRunner.purgeRequestQueue();

        responseQueue = queueBasedImplementationRunner.getResponseQueue();
        responseQueueUrl = queueBasedImplementationRunner.getResponseQueueUrl();
        queueBasedImplementationRunner.purgeResponseQueue();
    }

    @Given("^the broker is not available$")
    public void client_with_wrong_broker() {
        logAuditStream.clearLog();
        ImplementationRunnerConfig config = new ImplementationRunnerConfig()
                .setHostname("111")
                .setPort(PORT)
                .setUniqueId("X")
                .setAuditStream(logAuditStream)
                .setRequestTimeoutMillis(200);
        queueBasedImplementationRunnerBuilder = new QueueBasedImplementationRunner.Builder()
                .setConfig(config);
    }

    @Then("^the time to wait for requests is (\\d+)ms$")
    public void check_time(int expectedTimeout) {
        assertThat("The client request timeout has a different value.",
                queueBasedImplementationRunner.getRequestTimeoutMillis(), equalTo(expectedTimeout));
    }

    @Then("^the request queue is \"([^\"]*)\"$")
    public void check_request_queue(String expectedValue) {
        assertThat("Request queue has a different value.",
                requestQueue.getQueueName(), equalTo(expectedValue));
    }

    @Then("^the response queue is \"([^\"]*)\"$")
    public void check_response_queue(String expectedValue) {
        assertThat("Response queue has a different value.",
                responseQueue.getQueueName(), equalTo(expectedValue));
    }

    class RequestRepresentation {

        String payload;

    }

    @Given("^I receive the following requests:$")
    public void initialize_request_queue(List<RequestRepresentation> requests) throws Throwable {
        for (RequestRepresentation request : requests) {
            logToConsole("payload: " + request.payload);
            queueBasedImplementationRunner.send(requestQueueUrl, new ExecuteCommandEvent(request.payload));
        }
        initialRequestCount = requests.size();
        logToConsole("initial requests sent: " + initialRequestCount);
    }

    @Given("^I receive (\\d+) identical requests like:$")
    public void sent_loads_of_requests(int number, List<RequestRepresentation> requests) throws Throwable {
        for (int i = 0; i < number; i++) {
            for (RequestRepresentation request : requests) {
                logToConsole("payload: " + request.payload);
                queueBasedImplementationRunner.send(requestQueueUrl, new ExecuteCommandEvent(request.payload));
            }
        }
        initialRequestCount = requests.size() * number;
    }


    //~~~~~ Implementations

    private static final Map<String, UserImplementation> USER_IMPLEMENTATIONS = new HashMap<String, UserImplementation>() {{
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
        put("echo the request", params -> params[0]);
        put("some logic", params -> "ok");
        put("work for 600ms", params -> {
            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "OK";
        });
    }};

    private static UserImplementation asImplementation(String call) {
        if (USER_IMPLEMENTATIONS.containsKey(call)) {
            return USER_IMPLEMENTATIONS.get(call);
        } else {
            throw new IllegalArgumentException("Not a valid implementation reference: \"" + call + "\"");
        }
    }

    private static final Map<String, ClientAction> CLIENT_ACTIONS = new HashMap<String, ClientAction>() {{
        put("publish", ClientActions.publish());
        put("stop", ClientActions.stop());
        put("publish and stop", ClientActions.publishAndStop());
    }};

    private static ClientAction asAction(String actionName) {
        if (CLIENT_ACTIONS.containsKey(actionName)) {
            return CLIENT_ACTIONS.get(actionName);
        } else {
            throw new IllegalArgumentException("Not a valid action reference: \"" + actionName + "\"");
        }
    }

    class ProcessingRuleRepresentation {
        String method;
        String call;
        String action;
    }

    @When("^I go live with the following processing rules:$")
    public void go_live(List<ProcessingRuleRepresentation> listOfRules) {
        listOfRules.forEach((ruleLine) ->
                queueBasedImplementationRunnerBuilder.withSolutionFor(
                        ruleLine.method,
                        asImplementation(ruleLine.call),
                        asAction(ruleLine.action)
                )
        );
        queueBasedImplementationRunner = queueBasedImplementationRunnerBuilder.create();

        long timestampBefore = System.nanoTime();
        queueBasedImplementationRunner.run();
        long timestampAfter = System.nanoTime();
        processingTimeMillis = (timestampAfter - timestampBefore) / 1000000;
    }

    //~~~~~ Assertions

    @Then("^the client should consume all requests$")
    public void request_queue_empty() {
        assertThat("Requests have not been consumed",
                queueBasedImplementationRunner.getSize(requestQueue), equalTo(asLong(0)));
    }

    @Then("^the client should consume first request$")
    public void request_queue_less_than_one() {
        assertThat("Wrong number of requests have been consumed",
                queueBasedImplementationRunner.getSize(requestQueue),
                equalTo(asLong(initialRequestCount - 1)));
    }

    class ResponseRepresentation {
        String payload;
    }

    @And("^the client should publish the following responses:$")
    public void response_queue_contains_expected(List<ResponseRepresentation> expectedResponses) throws Throwable {
        List<String> expectedContents = expectedResponses.stream()
                .map(responseRepresentation -> responseRepresentation.payload)
                .collect(Collectors.toList());

        List<String> actualContents = queueBasedImplementationRunner.getReceivedMessages();
        assertThat("The responses are not correct", actualContents, equalTo(expectedContents));
    }

    class OutputRepresentation {
        String output;
    }

    @Then("^the client should display to console:$")
    public void the_client_should_display_to_console(List<OutputRepresentation> expectedOutputs) {
        String output = logAuditStream.getLog();
        for (OutputRepresentation expectedLine : expectedOutputs) {
            assertThat(output, containsString(expectedLine.output));
        }
        System.out.println(output);
    }

    @But("^the client should not display to console:$")
    public void the_client_should_not_display_to_console(List<String> rejectedOutputs) {
        String output = logAuditStream.getLog();
        for (String expectedLine : rejectedOutputs) {
            assertThat(output, not(containsString(expectedLine)));
        }
    }

    @Then("^the client should not consume any request$")
    public void request_queue_unchanged() throws Throwable {
        assertThat("The request queue has different size. The message has been consumed.",
                queueBasedImplementationRunner.getSize(requestQueue),
                equalTo(asLong(initialRequestCount)));
    }

    @And("^the client should not publish any response$")
    public void response_queue_unchanged() throws Throwable {
        assertThat("The response queue has different size. Messages have been published.",
                queueBasedImplementationRunner.getSize(responseQueue), equalTo(asLong(0)));
    }

    @Then("^I should get no exception$")
    public void I_should_get_no_exception() {
        System.out.println("Looking good");
    }

    @And("^the processing time should be lower than (\\d+)ms$")
    public void processingTimeShouldBeLowerThanMs(long threshold) {
        assertThat(processingTimeMillis, lessThan(threshold));
    }

    //~~~ Utils

    private static Long asLong(Integer value) {
        return (long) value;
    }

    private void logToConsole(String s) {
        if ((System.getenv("DEBUG") != null) && System.getenv("DEBUG").contains("true")) {
            System.out.println(s);
        }
    }
}
