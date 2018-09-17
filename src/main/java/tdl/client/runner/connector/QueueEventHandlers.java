package tdl.client.runner.connector;

import java.util.HashMap;
import java.util.Map;

public class QueueEventHandlers {
    private Map<String, HandleRule> handleMap;

    public QueueEventHandlers() {
        handleMap = new HashMap<>();
    }

    public <T> void put(Class<T> type, MessageConsumer<T> handler) throws EventProcessingException {
        QueueEvent annotation = type.getAnnotation(QueueEvent.class);
        if (annotation == null) {
            throw new EventProcessingException(type.getClass()+" not a QueueEvent");
        }
        handleMap.put(getKey(annotation.name(), annotation.version()), new HandleRule<>(type, handler));
    }

    public boolean canHandle(String eventName, String eventVersion) {
        String eventKey = getKey(eventName, eventVersion);
        return handleMap.containsKey(eventKey);
    }

    public HandleRule getRuleFor(String eventName, String eventVersion) {
        String eventKey = getKey(eventName, eventVersion);
        return handleMap.get(eventKey);
    }

    private static String getKey(String eventName, String eventVersion) {
        return eventName+"-"+eventVersion;
    }
}
