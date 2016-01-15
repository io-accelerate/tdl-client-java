package tdl.client.abstractions;

import tdl.client.actions.ClientAction;
import tdl.client.actions.PublishAndStopAction;

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

    public ProcessingRule getRuleFor(Request request) {
        String methodName = request.getMethodName();
        if (rules.containsKey(methodName)) {
            return rules.get(methodName);
        } else {
            throw new NoSuchMethodError(String.format("Method \"%s\" did not match any processing rule.", methodName));
        }
    }
}
