package factory.storage;

import factory.Storage;

import java.util.LinkedList;

public class SingleSpeciesStorage<T> implements Storage<T> {
    private final LinkedList<T> store = new LinkedList<>();
    private final int capacity;

    public SingleSpeciesStorage(int capacity) {
        this.capacity = capacity;
    }

    @Override
    synchronized public T getComponent() throws InterruptedException {
        if(store.isEmpty()){
            this.wait();
        }
        T component = store.pop();
        this.notify();
        return component;
    }

    @Override
    synchronized public void pushComponent(T component) throws InterruptedException {
        if(store.size() >= capacity){
            this.wait();
        }
        store.add(component);
        this.notify();
    }
}
