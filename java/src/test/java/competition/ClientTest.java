package competition;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by julianghionoiu on 13/06/2015.
 */
public class ClientTest {

    @Before
    public void setUp() throws Exception {
        // Before testing make sure activemq broker is up and running

        // External ActiveMq embedded into this repo

        // Clean both queues
        // Have messages ready in the request queue
    }

    @Test
    public void if_user_does_not_go_live_client_should_not_consume_or_publish_any_messages() throws Exception {
        // Client is set in development mode (not live)

        // Start client with correct implementation

        // Queues are untouched
    }

    @Test
    public void if_user_goes_live_client_should_process_all_messages() throws Exception {
        // Client is set to go live

        // Start client with correct implementation

        // All requests have been consumed
        // Response queue contains correct response
    }

    @Test
    public void returning_null_from_user_method_should_stop_all_processing() {
        // Client is set to go live

        // Start client with null implementation

        // Queues are untouched
    }

    @Test
    public void throwing_exceptions_from_user_method_should_stop_all_processing() throws Exception {
        // Client is set to go live

        // Start client with faulty

        // Queues are untouched
    }

    //~~~ Utils

    // assertQueuesAreUntouched
}