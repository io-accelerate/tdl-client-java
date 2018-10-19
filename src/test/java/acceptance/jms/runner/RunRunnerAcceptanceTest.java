package acceptance.jms.runner;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty"}, features = {"src/test/resources/acceptance/runner"}, glue = {"acceptance.jms.runner"})
public class RunRunnerAcceptanceTest {
}
