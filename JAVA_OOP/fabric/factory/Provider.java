package factory;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class Provider {
    private volatile int period; //period in ms
    private int counterProduct = 0;
    ManufacturingProcess manufacturingProcess;
    Timer timer;

    Storage<? super Product> storage;

    public Provider(int period, Class<? extends Product> providingProduct, Storage<? super Product> storage){
        this.period = period;
        this.storage = storage;
        timer = new Timer(this.getClass().toString());
        manufacturingProcess = new ManufacturingProcess(providingProduct.toString());
    }

    public void startSupplies(Product productToSupply){

    }

    private class ManufacturingProcess extends TimerTask{
        String productType;

        public ManufacturingProcess(String product){
            productType = product;
        }

        @Override
        public void run() {
            try {
                Class<?> productClass = Class.forName(productType);
                Product product = (Product) productClass.getConstructor(int.class).newInstance(counterProduct);
                storage.pushComponent(product);
                ++counterProduct;
            } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
                     IllegalAccessException | NoSuchMethodException | InterruptedException e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
            }
        }
    }
}
