package tdl.client.queue;

import org.slf4j.LoggerFactory;
import tdl.client.queue.abstractions.ProcessingRule;
import tdl.client.queue.abstractions.Request;
import tdl.client.queue.abstractions.UserImplementation;
import tdl.client.queue.abstractions.response.FatalErrorResponse;
import tdl.client.queue.abstractions.response.Response;
import tdl.client.queue.abstractions.response.ValidResponse;
import tdl.client.queue.actions.ClientAction;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by julianghionoiu on 17/10/2015.
 */
public class ProcessingRules {
    private final Map<String, ProcessingRule> rules;

    //~~~ Builders

    ProcessingRules() {
        rules = new HashMap<>();
    }

    private void add(String methodName, UserImplementation userImplementation, ClientAction clientAction) {
        logToConsole("           ProcessingRules add");
        rules.put(methodName, new ProcessingRule(userImplementation, clientAction));
    }

    private static void logToConsole(String s) {
        if (new File("DEBUG").exists()) {
            System.out.println(s);
        }
    }

    public ProcessingRuleBuilder on(String methodName) {
        logToConsole("           ProcessingRules on");
        return new ProcessingRuleBuilder(this, methodName);
    }

    public static class ProcessingRuleBuilder {

        private final ProcessingRules instance;
        private final String methodName;
        private UserImplementation userImplementation;

        ProcessingRuleBuilder(ProcessingRules instance, String methodName) {
            logToConsole("           ProcessingRuleBuilder creation");
            this.instance = instance;
            this.methodName = methodName;
        }

        ProcessingRuleBuilder call(UserImplementation userImplementation) {
            logToConsole("           ProcessingRuleBuilder call");
            this.userImplementation = userImplementation;
            return this;
        }

        void then(ClientAction clientAction) {
            logToConsole("           ProcessingRuleBuilder then");
            instance.add(methodName, userImplementation, clientAction);
        }
    }
    //~~~ Accessors

    Response getResponseFor(Request request) {
        logToConsole("           ProcessingRules getResponseFor [start]");
        ProcessingRule rule;
        String methodName = request.getMethodName();
        if (rules.containsKey(methodName)) {
            rule = rules.get(methodName);
        } else {
            String message = String.format("method '%s' did not match any processing rule", methodName);
            return new FatalErrorResponse(message);
        }

        Response response;
        try {
            Object result = rule.getUserImplementation().process(request.getParams());
            response = new ValidResponse(request.getId(), result, rule.getClientAction());
        } catch (Exception e) {
            String message = "user implementation raised exception";
            LoggerFactory.getLogger(ProcessingRules.class).warn(message, e);
            return new FatalErrorResponse(message);
        }

        logToConsole("           ProcessingRules getResponseFor [end]");
        return response;
    }

    @Override
    public String toString() {
        String rulesAsString = rules.entrySet()
                .stream()
                .map(entry -> entry.getKey() + " - " + entry.getValue() + "\n")
                .collect(Collectors.joining(", "));
        return "ProcessingRules{" +
                "rules=" + rulesAsString +
                '}';
    }
}
