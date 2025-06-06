package acceptance.queue;

import acceptance.SingletonTestBroker;
import io.cucumber.java.DataTableType;
import io.cucumber.java.en.*;
import io.accelerate.client.audit.StdoutAuditStream;
import io.accelerate.client.queue.ImplementationRunnerConfig;
import io.accelerate.client.queue.QueueBasedImplementationRunner;
import io.accelerate.client.queue.abstractions.UserImplementation;
import utils.jmx.broker.RemoteJmxQueue;
import utils.logging.LogAuditStream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class QueueSteps {
    private final SingletonTestBroker broker;

    // Test broker location
    private static final String HOSTNAME = "localhost";
    private static final int PORT = 21616;

    // Variables set by the background tasks
    private RemoteJmxQueue requestQueue;
    private RemoteJmxQueue responseQueue;
    private QueueBasedImplementationRunner.Builder queueBasedImplementationRunnerBuilder;

    //Testing utils
    private int initialRequestCount;
    private long processingTimeMillis;
    private final LogAuditStream logAuditStream;

    public QueueSteps(SingletonTestBroker broker) {
        this.broker = broker;
        this.logAuditStream = new LogAuditStream(new StdoutAuditStream());
        this.initialRequestCount = 0;
        this.processingTimeMillis = 0;
    }

    //~~~~~ Setup

    @Given("^I start with a clean broker having a request and a response queue$")
    public void broker_setup() throws Throwable {
        logAuditStream.clearLog();

        requestQueue = broker.addQueue("some-user-req");
        requestQueue.purge();

        responseQueue = broker.addQueue("some-user-resp");
        responseQueue.purge();
    }

    @And("^a client that connects to the queues$")
    public void client_setup() {
        logAuditStream.clearLog();

        ImplementationRunnerConfig config = new ImplementationRunnerConfig().setHostname(HOSTNAME)
                .setPort(PORT)
                .setRequestQueueName(requestQueue.getName())
                .setResponseQueueName(responseQueue.getName())
                .setAuditStream(logAuditStream);
        queueBasedImplementationRunnerBuilder = new QueueBasedImplementationRunner.Builder()
                .setConfig(config);
    }

    @Given("^the broker is not available$")
    public void client_with_wrong_broker() {
        logAuditStream.clearLog();
        ImplementationRunnerConfig config = new ImplementationRunnerConfig()
                .setHostname("111")
                .setPort(PORT)
                .setRequestQueueName("X")
                .setRequestQueueName("Y")
                .setAuditStream(logAuditStream)
                .setRequestTimeoutMillis(200);
        queueBasedImplementationRunnerBuilder = new QueueBasedImplementationRunner.Builder()
                .setConfig(config);
    }

    @Then("^the time to wait for requests is (\\d+)ms$")
    public void check_time(int expectedTimeout) {
        assertThat("The client request timeout has a different value.",
                queueBasedImplementationRunnerBuilder.create().getRequestTimeoutMillis(), equalTo(expectedTimeout));
    }

    public record RequestRepresentation (String payload) {}

    @DataTableType
    public RequestRepresentation requestRepresentation(Map<String, String> entry) {
        return new RequestRepresentation(escapeNewlines(entry.get("payload")));
    }

    private String escapeNewlines(String payload) {
        return payload.replace("\n", "\\n");
    }

    @Given("^I receive the following requests:$")
    public void initialize_request_queue(List<RequestRepresentation> requests) throws Throwable {
        for (RequestRepresentation request : requests) {
            requestQueue.sendTextMessage(request.payload);
        }
        initialRequestCount = requests.size();
    }
    @Given("^I receive (\\d+) identical requests like:$")
    public void sent_loads_of_requests(int number, List<RequestRepresentation> requests) throws Throwable {
        for (int i = 0; i < number; i++) {
            for (RequestRepresentation request : requests) {
                requestQueue.sendTextMessage(request.payload);
            }
        }
        initialRequestCount = requests.size() * number;
    }


    //~~~~~ Implementations

    private record TestItem(String field1, Integer field2) {}
        

    @SuppressWarnings("CodeBlock2Expr")
    private static final Map<String, UserImplementation> USER_IMPLEMENTATIONS = new HashMap<>() {{
        put("add two numbers", params -> {
            Integer x = params.get(0).getAsInteger();
            Integer y = params.get(1).getAsInteger();
            return x + y;
        });
        put("increment number", params -> {
            Integer x = params.getFirst().getAsInteger();
            return x + 1;
        });
        put("return null", params -> null);
        put("throw exception", param -> {
            throw new IllegalStateException("faulty user code");
        });
        put("replay the value", params -> params.getFirst().getAsObject(Object.class));
        put("sum the elements of an array", params -> {
            int sum = 0;
            for (Integer element : params.getFirst().getAsListOf(Integer.class)) {
                sum += element;
            }
            return sum;
        });
        put("generate array of integers", params -> {
            int start_incl = params.get(0).getAsInteger();
            int end_excl = params.get(1).getAsInteger();
            return IntStream.range(start_incl, end_excl).boxed().collect(Collectors.toList());
        });
        put("some logic", params -> "ok");
        put("work for 600ms", params -> {
            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "OK";
        });
        put("concatenate fields as string", params -> {
            TestItem testItem = params.getFirst().getAsObject(TestItem.class);
            return testItem.field1 + testItem.field2;
        });
        put("build an object with two fields", params -> {
            String field1 = params.get(0).getAsString();
            Integer field2 = params.get(1).getAsInteger();
            return new TestItem(field1, field2);
        });
        put("retrieve a value from a map", params -> {
            return params.getFirst().getAsMapOf(String.class).get("key1");
        });
    }};


    private static UserImplementation asImplementation(String call) {
        if (USER_IMPLEMENTATIONS.containsKey(call)) {
            return USER_IMPLEMENTATIONS.get(call);
        } else {
            throw new IllegalArgumentException("Not a valid implementation reference: \"" + call+"\"");
        }
    }

    public record ProcessingRuleRepresentation(String method, String call) {}

    @DataTableType
    public ProcessingRuleRepresentation processingRuleRepresentation(Map<String, String> entry) {
        return new ProcessingRuleRepresentation(entry.get("method"), entry.get("call"));
    }

    @When("^I go live with the following processing rules:$")
    public void go_live(List<ProcessingRuleRepresentation> listOfRules) {
        listOfRules.forEach((ruleLine) ->
            queueBasedImplementationRunnerBuilder.withSolutionFor(
                    ruleLine.method,
                    asImplementation(ruleLine.call)
            )
        );
        QueueBasedImplementationRunner queueBasedImplementationRunner = queueBasedImplementationRunnerBuilder.create();

        long timestampBefore = System.nanoTime();
        queueBasedImplementationRunner.run();
        long timestampAfter = System.nanoTime();
        processingTimeMillis = (timestampAfter - timestampBefore) / 1000000;
    }

    //~~~~~ Assertions

    @Then("^the client should consume all requests$")
    public void request_queue_empty() throws Throwable {
        assertThat("Requests have not been consumed",requestQueue.getSize(), equalTo(asLong(0)));
    }

    @Then("^the client should consume one request$")
    public void request_queue_less_than_one() throws Throwable {
        assertThat("Wrong number of requests have been consumed",requestQueue.getSize(),
                equalTo(asLong(initialRequestCount-1)));
    }

    record ResponseRepresentation(String payload) {}

    @DataTableType
    public ResponseRepresentation responseRepresentation(Map<String, String> entry) {
        return new ResponseRepresentation(entry.get("payload"));
    }

    @And("^the client should publish one response$")
    public void response_queue_is_one() throws Throwable {
        assertThat("Wrong number of responses have been received",responseQueue.getSize(),
                equalTo(asLong(1)));
    }

    @And("^the client should publish the following responses:$")
    public void response_queue_contains_expected(List<ResponseRepresentation> expectedResponses) throws Throwable {
        List<String> expectedContents = expectedResponses.stream()
                .map(responseRepresentation -> responseRepresentation.payload)
                .collect(Collectors.toList());
        assertThat("The responses are not correct",responseQueue.getMessageContents(), equalTo(expectedContents));
    }

    record OutputRepresentation(String output) {}

    @DataTableType
    public OutputRepresentation outputRepresentation(Map<String, String> entry) {
        return new OutputRepresentation(entry.get("output"));
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
                requestQueue.getSize(), equalTo(asLong(initialRequestCount)));
    }

    @And("^the client should not publish any response$")
    public void response_queue_unchanged() throws Throwable {
        assertThat("The response queue has different size. Messages have been published.",
                responseQueue.getSize(), equalTo(asLong(0)));
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
}
