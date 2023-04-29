package factory.provision;

import factory.InteractivePerformer;
import factory.Product;
import factory.Storage;

import java.lang.reflect.InvocationTargetException;

public class Provider <T extends Product> implements InteractivePerformer {
    private volatile int period; //period in ms
    private int counterProduct = 0;
    Thread executor;

    Storage<T> storage;

    public Provider(int period, Class<? extends Product> providingProduct, Storage<T> storage, int providerSerialNumber){
        this.period = period;
        this.storage = storage;
        executor = new Thread(new ManufacturingProcess(providingProduct.getTypeName()));
        executor.setName("Provider of "+providingProduct+" #"+providerSerialNumber);
    }

    @Override
    public void startPerform(){
        executor.start();
    }

    @Override
    public void stopPerform(){
        executor.interrupt();
    }

    @Override
    public void changePeriod(int newPeriod) {
        period = newPeriod;
    }

    private class ManufacturingProcess implements Runnable{
        String productType;

        public ManufacturingProcess(String product){
            productType = product;
        }

        @Override
        public void run() {
            while(!Thread.interrupted()) {
                try {
                    Thread.sleep(period);
                    doCreation();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        @SuppressWarnings("unchecked")
        private void doCreation() {
            try {
                Class<?> productClass = Class.forName(productType);
                T product = (T) productClass.getConstructor(int.class).newInstance(counterProduct);
                storage.pushComponent(product);
                ++counterProduct;
            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                     InstantiationException | IllegalAccessException e){
                e.printStackTrace();
            }
        }

    }
}
