package io.accelerate.client.queue;

import org.slf4j.LoggerFactory;
import io.accelerate.client.queue.abstractions.ProcessingRule;
import io.accelerate.client.queue.abstractions.Request;
import io.accelerate.client.queue.abstractions.UserImplementation;
import io.accelerate.client.queue.abstractions.response.FatalErrorResponse;
import io.accelerate.client.queue.abstractions.response.Response;
import io.accelerate.client.queue.abstractions.response.ValidResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by julianghionoiu on 17/10/2015.
 */
public class ProcessingRules {
    private final Map<String, ProcessingRule> rules;

    //~~~ Builders

    ProcessingRules() {
        rules = new HashMap<>();
    }

    private void add(String methodName, UserImplementation userImplementation) {
        rules.put(methodName, new ProcessingRule(userImplementation));
    }

    public ProcessingRuleBuilder on(String methodName) {
        return new ProcessingRuleBuilder(this, methodName);
    }

    public static class ProcessingRuleBuilder {

        private final ProcessingRules instance;
        private final String methodName;
        private UserImplementation userImplementation;

        ProcessingRuleBuilder(ProcessingRules instance, String methodName) {
            this.instance = instance;
            this.methodName = methodName;
        }

        ProcessingRuleBuilder call(UserImplementation userImplementation) {
            this.userImplementation = userImplementation;
            return this;
        }

        void build() {
            instance.add(methodName, userImplementation);
        }
    }
    //~~~ Accessors

    Response getResponseFor(Request request) {
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
            response = new ValidResponse(request.getId(), result, request.getObjectMapper());
        } catch (Exception e) {
            String message = "user implementation raised exception";
            LoggerFactory.getLogger(ProcessingRules.class).warn(message, e);
            return new FatalErrorResponse(message);
        }
        return response;
    }

}
