package tdl.client.runner.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;
import tdl.client.runner.connector.QueueEvent;

@Getter
@ToString
@QueueEvent(name = "sourceCodeUpdated", version = "0.1")
public class ExecuteCommandEvent {
    private final String payload;

    public ExecuteCommandEvent(@JsonProperty("payload") String payload) {
        this.payload = payload;
    }
}
