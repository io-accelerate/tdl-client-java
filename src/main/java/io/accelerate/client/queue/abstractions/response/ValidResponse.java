package io.accelerate.client.queue.abstractions.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.accelerate.client.audit.PresentationUtils;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class ValidResponse implements Response {
    private final String id;
    private final Object result;
    private final PresentationUtils presentationUtils;

    public ValidResponse(String id, Object result, ObjectMapper objectMapper) {
        this.id = id;
        this.result = result;
        presentationUtils = new PresentationUtils(objectMapper);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Object getResult() {
        return result;
    }

    //~~~ Pretty print
    @Override
    public String getAuditText() {
        return String.format("resp = %s", presentationUtils.toDisplayableResponse(result));
    }

}
