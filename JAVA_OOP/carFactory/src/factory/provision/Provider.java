package factory.provision;

import factory.InteractivePerformer;
import factory.Product;
import factory.Storage;
import factory.product.Accessory;
import factory.product.Body;
import factory.product.Engine;

import java.util.logging.Level;

import static factory.Factory.fabricConditionDescriptor;
import static factory.Factory.logger;

public class Provider <T extends Product> implements InteractivePerformer {
    private volatile int period; //period in ms
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
                    break;
                }
            }
        }

        @SuppressWarnings("unchecked")
        private void doCreation() {
            try {
                Class<?> productClass = Class.forName(productType);
                T product = (T) productClass.newInstance();
                storage.pushComponent(product);
                if(product instanceof Engine) {
                    fabricConditionDescriptor.incrementEngineProductionAmount();
                    if(logger != null){
                        logger.log(Level.INFO, "\033[95m Product "+productType+"<ID="+product.getId()+"> was created. \u001b[0m");
                    }
                } else if(product instanceof Accessory){
                    fabricConditionDescriptor.incrementAccessoryProductionAmount();
                    if(logger != null){
                        logger.log(Level.INFO, "\u001b[34m Product "+productType+"<ID="+product.getId()+"> was created. \u001b[0m");
                    }
                } else if(product instanceof Body){
                    fabricConditionDescriptor.incrementBodyProductionAmount();
                    if(logger != null){
                        logger.log(Level.INFO, "\u001b[32m Product "+productType+"<ID="+product.getId()+"> was created. \u001b[0m");
                    }
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e){
                e.printStackTrace();
            }
        }

    }
}