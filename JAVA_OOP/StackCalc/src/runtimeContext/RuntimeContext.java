package runtimeContext;

public interface RuntimeContext<T> {
    /**
     * DEFINE A 4
     * DEFINE A 5
     * PUSH A
     * PRINT -> 5.0
     * ------------
     * DEFINE 45 35
     * PUSH 45
     * PRINT -> 35.0
     */
    public void define(String key, T value);

    public T getIfDefined(String key);
}
