package factory.storage;

import factory.Storage;

import java.util.LinkedList;

public class SingleSpeciesStorage<E> implements Storage<E> {
    private final LinkedList<E> store = new LinkedList<>();
    private final int capacity;

    public SingleSpeciesStorage(int capacity) {
        this.capacity = capacity;
    }

    @Override
    synchronized public E getComponent(){
        while(store.isEmpty()){
            try {
                this.wait();
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }

        E component = store.pop();
        this.notifyAll();
        return component;
    }

    @Override
    synchronized public void pushComponent(E component) {
        while(store.size() >= capacity){
            try {
                this.wait();
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }

        store.add(component);
        this.notifyAll();
    }

    @Override
    public int getCapacity(){
        return capacity;
    }
}
