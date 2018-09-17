package tdl.client.runner.connector;

@FunctionalInterface
public interface MessageConsumer<T> {
    void accept(T t) throws Exception;
}
