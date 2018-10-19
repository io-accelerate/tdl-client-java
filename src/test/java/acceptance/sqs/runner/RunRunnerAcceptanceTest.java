package acceptance.sqs.runner;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty"}, features = {"src/test/resources/acceptance/runner"}, glue = {"acceptance.sqs.runner"})
public class RunRunnerAcceptanceTest {
}
