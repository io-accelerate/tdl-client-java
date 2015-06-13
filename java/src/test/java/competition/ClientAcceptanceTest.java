package competition;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import utils.jmx.RemoteJmxQueue;
import utils.jmx.testing.ActiveMQBrokerRule;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by julianghionoiu on 13/06/2015.
 */
public class ClientAcceptanceTest {
    List<String> REQUESTS = Lists.newArrayList(
            "X1, 0",
            "X2, 5");
    List<String> EXPECTED_RESPONSES = Lists.newArrayList(
            "X1, 1",
            "X2, 6");

    @ClassRule
    public static ActiveMQBrokerRule broker = new ActiveMQBrokerRule("localhost", 20011, "TEST.BROKER");

    //Queues
    RemoteJmxQueue requestQueue;
    RemoteJmxQueue responseQueue;

    @Before
    public void setUp() throws Exception {
        //Given we have a couple of requests waiting
        requestQueue = broker.addQueue("test.req");
        requestQueue.purge();
        requestQueue.sendTextMessage("X1, 0");
        requestQueue.sendTextMessage("X2, 5");

        //And no responses
        responseQueue = broker.addQueue("test.resp");
        responseQueue.purge();
    }

    @Ignore("Work in progress")
    @Test
    public void if_user_goes_live_client_should_process_all_messages() throws Exception {
        // Client is set to go live

        // Start client with correct implementation

        assertThat(requestQueue.getSize(), equalTo(asLong(0)));
        assertThat(responseQueue.getMessageContents(), equalTo(EXPECTED_RESPONSES));
    }

    @Test
    public void if_user_does_not_go_live_client_should_not_consume_or_publish_any_messages() throws Exception {
        // Client is set in development mode (not live)

        // Start client with correct implementation

        // Queues are untouched

        assertQueuesAreUntouched();
    }

    @Test
    public void returning_null_from_user_method_should_stop_all_processing() throws Exception {
        // Client is set to go live

        // Start client with null implementation

        // Queues are untouched
        assertQueuesAreUntouched();
    }

    @Test
    public void throwing_exceptions_from_user_method_should_stop_all_processing() throws Exception {
        // Client is set to go live

        // Start client with faulty

        // Queues are untouched
        assertQueuesAreUntouched();
    }

    //~~~ Utils

    private void assertQueuesAreUntouched() throws Exception {
        assertThat(requestQueue.getSize(), equalTo(asLong(REQUESTS.size())));
        assertThat(responseQueue.getSize(), equalTo(asLong(0)));
    }

    private static Long asLong(Integer value) {
        return new Long(value);
    }
}