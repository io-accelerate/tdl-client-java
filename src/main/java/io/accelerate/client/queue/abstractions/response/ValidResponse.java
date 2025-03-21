package io.accelerate.client.queue.abstractions.response;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public record ValidResponse(String id, Object result) implements Response {
    @Override
    public boolean isError() {
        return false;
    }
}
