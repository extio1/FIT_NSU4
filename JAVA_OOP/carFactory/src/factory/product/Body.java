package factory.product;

import factory.Product;

import java.util.concurrent.atomic.AtomicInteger;

public class Body implements Product {
    private static final AtomicInteger id = new AtomicInteger(0);
    public Body(){
        id.incrementAndGet();
    }

    @Override
    public int getId() {
        return id.get();
    }
}
