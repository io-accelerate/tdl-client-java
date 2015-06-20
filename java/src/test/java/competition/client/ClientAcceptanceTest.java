package competition.client;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import utils.jmx.broker.RemoteJmxQueue;
import utils.jmx.broker.testing.ActiveMQBrokerRule;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Created by julianghionoiu on 13/06/2015.
 */
public class ClientAcceptanceTest {
    private static final List<String> REQUESTS = Lists.newArrayList(
            "X1, 0, 1",
            "X2, 5, 6");
    private static final List<String> EXPECTED_RESPONSES = Lists.newArrayList(
            "X1, 1",
            "X2, 11");
    private static final String FIRST_EXPECTED_TEXT  = "id = X1, req = 0, 1, resp = 1";
    private static final String SECOND_EXPECTED_TEXT = "id = X2, req = 5, 6, resp = 11";
    private static final List<String> EXPECTED_DISPLAYED_TEXT = Lists.newArrayList(
            FIRST_EXPECTED_TEXT, SECOND_EXPECTED_TEXT);

    private static final UserImplementation CORRECT_SOLUTION = serializedParams -> {
        String[] params = serializedParams.split(", ");
        Integer x = Integer.parseInt(params[0]);
        Integer y = Integer.parseInt(params[1]);
        return x + y;
    };


    private static final int JMX_PORT = 20011;
    private static final int OPENWIRE_PORT = 21616;
    public static final String BROKER_URL = "tcp://localhost:"+OPENWIRE_PORT;

    @ClassRule
    public static ActiveMQBrokerRule broker = new ActiveMQBrokerRule("localhost", JMX_PORT, "TEST.BROKER");

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();


    //Queues
    RemoteJmxQueue requestQueue;
    RemoteJmxQueue responseQueue;
    Client client;

    @Before
    public void setUp() throws Exception {
        String test = "test";

        //Given we have a couple of requests waiting
        requestQueue = broker.addQueue(test +".req");
        requestQueue.purge();
        for (String request : REQUESTS) {
            requestQueue.sendTextMessage(request);
        }

        //And no responses
        responseQueue = broker.addQueue(test +".resp");
        responseQueue.purge();

        //Initialize client
        client = new Client(BROKER_URL, test);
    }

    //~~~~ Go live

    @Test
    public void if_user_goes_live_client_should_process_all_messages() throws Exception {

        client.goLiveWith(CORRECT_SOLUTION);

        assertThat("Requests have not been consumed",requestQueue.getSize(), equalTo(asLong(0)));
        assertThat("The responses are not correct",responseQueue.getMessageContents(), equalTo(EXPECTED_RESPONSES));
    }

    @Test
    public void a_run_should_show_the_messages_and_the_responses() throws Exception {

        client.goLiveWith(CORRECT_SOLUTION);

        String output = systemOutRule.getLog();
        for (String expectedLine : EXPECTED_DISPLAYED_TEXT) {
            assertThat(output, containsString(expectedLine));
        }
    }

    @Test
    public void returning_null_from_user_method_should_stop_all_processing() throws Exception {

        client.goLiveWith(param -> null);

        assertQueuesAreUntouched();
    }

    @Test
    public void throwing_exceptions_from_user_method_should_stop_all_processing() throws Exception {

        client.goLiveWith(param -> {
            throw new IllegalStateException("faulty user code");
        });

        assertQueuesAreUntouched();
    }

    //~~~~ Trial run

    @Test
    public void a_trial_run_should_only_show_the_first_message_and_the_response() throws Exception {

        client.trialRunWith(CORRECT_SOLUTION);

        String output = systemOutRule.getLog();
        assertThat("Expected displayed response and request",
                output, containsString(FIRST_EXPECTED_TEXT));
        assertThat("Should not have displayed the next message",
                output, not(containsString(SECOND_EXPECTED_TEXT)));
    }

    @Test
    public void if_user_does_a_trial_run_should_not_consume_or_publish_any_messages() throws Exception {

        client.trialRunWith(CORRECT_SOLUTION);

        assertQueuesAreUntouched();
    }

    //~~~ Utils

    private void assertQueuesAreUntouched() throws Exception {
        assertThat("The request queue has different size. Messages have been consumed.",
                requestQueue.getSize(), equalTo(asLong(REQUESTS.size())));
        assertThat("The response queue has different size. Messages have been published.",
                responseQueue.getSize(), equalTo(asLong(0)));
    }

    private static Long asLong(Integer value) {
        return (long) value;
    }
}