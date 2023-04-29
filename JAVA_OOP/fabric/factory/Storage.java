package factory;

public interface Storage <E>{
    E getComponent();
    void pushComponent(E component);
    int getCapacity();
}
