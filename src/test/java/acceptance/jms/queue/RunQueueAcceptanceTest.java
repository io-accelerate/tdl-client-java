package acceptance.jms.queue;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty"}, glue = {"acceptance.jms.queue"})
public class RunQueueAcceptanceTest {
}
