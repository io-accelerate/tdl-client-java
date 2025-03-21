package io.accelerate.client.queue.abstractions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.util.List;

public class ParamAccessor {
    private final JsonNode jsonNode;
    private final ObjectMapper objectMapper;

    public ParamAccessor(JsonNode jsonNode, ObjectMapper objectMapper) {
        this.jsonNode = jsonNode;
        this.objectMapper = objectMapper;
    }
    
    public String getAsString() {
        return getAsObject(String.class);
    }

    public Integer getAsInteger() {
        return getAsObject(Integer.class);
    }

    public boolean isArray() {
        return jsonNode.isArray();
    }
    
    public <T> List<T> getAsListOf(Class<T> classType) {
        try {
            return objectMapper.treeToValue(jsonNode, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize jsonNode to List of " + classType.getName(), e);
        }
    }

    public <T> T getAsObject(Class<T> itemClass) {
        try {
            return objectMapper.treeToValue(jsonNode, itemClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize jsonNode to " + itemClass.getName(), e);
        }
    }
}
