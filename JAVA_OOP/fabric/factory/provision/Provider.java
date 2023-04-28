package factory.provision;

import factory.Product;
import factory.Storage;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class Provider <T extends Product> {
    private volatile int period; //period in ms
    private int counterProduct = 0;
    ManufacturingProcess manufacturingProcess;
    Thread executor;

    Storage<T> storage;

    public Provider(int period, Class<? extends Product> providingProduct, Storage<T> storage){
        this.period = period;
        this.storage = storage;
        executor = new Thread(new ManufacturingProcess(providingProduct.toString()));
    }

    public void startSupplies(){
        executor.start();
    }

    public void stopSupplies(){
        executor.interrupt();
    }

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
                    doJob();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        @SuppressWarnings("unchecked")
        private void doJob() {
            try {
                Class<?> productClass = Class.forName(productType);
                T product = (T) productClass.getConstructor(int.class).newInstance(counterProduct);
                storage.pushComponent(product);
                ++counterProduct;
            } catch (ClassNotFoundException | NoSuchMethodException | InterruptedException |
                     InvocationTargetException | InstantiationException | IllegalAccessException e){
                e.printStackTrace();
            }
        }

    }
}
