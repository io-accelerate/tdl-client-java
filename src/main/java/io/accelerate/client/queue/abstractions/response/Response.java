package io.accelerate.client.queue.abstractions.response;

/**
 * Created by julianghionoiu on 17/01/2016.
 */
public interface Response {
    String id();

    Object result();
    
    boolean isError();
}
