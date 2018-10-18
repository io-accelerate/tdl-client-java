package acceptance.sqs.runner;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty"}, glue = {"acceptance.sqs.runner"})
public class RunRunnerAcceptanceTest {
}
