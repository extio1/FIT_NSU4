package factory.storage;

import factory.Product;
import factory.Storage;
import factory.product.Accessory;
import factory.product.Body;
import factory.product.Car;
import factory.product.Engine;

import java.util.LinkedList;

import static factory.Factory.fabricConditionDescriptor;


public class SingleSpeciesStorage<E extends Product> implements Storage<E> {
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
                Thread.currentThread().interrupt();
                return null;
            }
        }

        E component = store.poll();

        if(component instanceof Engine){
            fabricConditionDescriptor.decrementEngineCurrCapacity();
        } else if(component instanceof Car) {
            fabricConditionDescriptor.decrementCarCurrCapacity();
        } else if(component instanceof Accessory){
            fabricConditionDescriptor.decrementAccessoryCurrCapacity();
        } else if(component instanceof Body){
            fabricConditionDescriptor.decrementBodyCurrCapacity();
        }

        this.notifyAll();
        return component;
    }

    @Override
    synchronized public void pushComponent(E component) {
        while(store.size() >= capacity){
            try {
                this.wait();
            } catch (InterruptedException e){
                Thread.currentThread().interrupt();
                return;
            }
        }

        store.offer(component);

        if(component instanceof Engine){
            fabricConditionDescriptor.incrementEngineCurrCapacity();
        } else if(component instanceof Car) {
            fabricConditionDescriptor.incrementCarCurrCapacity();
        } else if(component instanceof Accessory){
            fabricConditionDescriptor.incrementAccessoryCurrCapacity();
        } else if(component instanceof Body){
            fabricConditionDescriptor.incrementBodyCurrCapacity();
        }

        this.notifyAll();
    }

    @Override
    public int getCapacity(){
        return capacity;
    }
}
