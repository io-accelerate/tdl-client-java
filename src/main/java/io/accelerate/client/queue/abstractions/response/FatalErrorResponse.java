package io.accelerate.client.queue.abstractions.response;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class FatalErrorResponse implements Response {
    private final String message;

    public FatalErrorResponse(String message) {
        this.message = message;
    }

    @Override
    public String id() {
        return "error";
    }

    @Override
    public Object result() {
        return message;
    }

    @Override
    public boolean isError() {
        return true;
    }
}
