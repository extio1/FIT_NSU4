package factory.product;

import factory.Product;

import java.util.concurrent.atomic.AtomicInteger;

public class Accessory implements Product {
    private static final AtomicInteger id = new AtomicInteger(0);
    public Accessory(){
        id.incrementAndGet();
    }

    @Override
    public int getId() {
        return id.get();
    }
}
