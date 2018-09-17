package tdl.client.runner.connector;

public class HandleRule<T> {
    private final Class<T> type;
    private final MessageConsumer<T> consumer;

    HandleRule(Class<T> type, MessageConsumer<T> consumer) {
        this.type = type;
        this.consumer = consumer;
    }

    public Class<T> getType() {
        return type;
    }

    public MessageConsumer<T> getConsumer() {
        return consumer;
    }
}
