package io.accelerate.client.queue.serialization;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

/**
 * Created by julianghionoiu on 10/01/2016.
 */
public record JsonRpcRequest(String method, List<JsonNode> params, String id) {
}
