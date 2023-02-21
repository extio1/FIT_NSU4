package runtimeContext;

public interface RuntimeContext<T> {
    public void define(String key, T value);
    public T getIfDefined(String key);
}
