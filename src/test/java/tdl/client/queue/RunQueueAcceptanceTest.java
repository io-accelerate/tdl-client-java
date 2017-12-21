package tdl.client.queue;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty"}, glue = {"tdl.client.queue"})
public class RunQueueAcceptanceTest {
}
