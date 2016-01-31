package tdl.client;

import org.slf4j.LoggerFactory;
import tdl.client.abstractions.ProcessingRule;
import tdl.client.abstractions.Request;
import tdl.client.abstractions.UserImplementation;
import tdl.client.abstractions.response.FatalErrorResponse;
import tdl.client.abstractions.response.Response;
import tdl.client.abstractions.response.ValidResponse;
import tdl.client.actions.ClientAction;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by julianghionoiu on 17/10/2015.
 */
public class ProcessingRules {
    private final Map<String, ProcessingRule> rules;

    //~~~ Builders

    public ProcessingRules() {
        rules = new HashMap<>();
    }

    public void add(String methodName, UserImplementation userImplementation, ClientAction clientAction) {
        rules.put(methodName, new ProcessingRule(userImplementation, clientAction));
    }

    public ProcessingRuleBuilder on(String methodName) {
        return new ProcessingRuleBuilder(this, methodName);
    }

    public static class ProcessingRuleBuilder {

        private final ProcessingRules instance;
        private final String methodName;
        private UserImplementation userImplementation;

        public ProcessingRuleBuilder(ProcessingRules instance, String methodName) {
            this.instance = instance;
            this.methodName = methodName;
        }

        public ProcessingRuleBuilder call(UserImplementation userImplementation) {
            this.userImplementation = userImplementation;
            return this;
        }

        public void then(ClientAction clientAction) {
            instance.add(methodName, userImplementation, clientAction);
        }
    }
    //~~~ Accessors

    public Response getResponseFor(Request request) {
        ProcessingRule rule;
        String methodName = request.getMethodName();
        if (rules.containsKey(methodName)) {
            rule = rules.get(methodName);
        } else {
            String message = String.format("method \"%s\" did not match any processing rule", methodName);
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
        return response;
    }

}
