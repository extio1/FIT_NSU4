package factory;

public interface Storage <T>{
    T getComponent() throws InterruptedException;
    void pushComponent(T component) throws InterruptedException;
}
