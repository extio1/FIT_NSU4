package factory.product;

import factory.Product;

import java.util.concurrent.atomic.AtomicInteger;

public class Car implements Product {
    private static final AtomicInteger id = new AtomicInteger(0);
    Engine engine;
    Body body;
    Accessory accessory;

    public Car(Engine engine, Body body, Accessory accessory) {
        id.incrementAndGet();
        this.engine = engine;
        this.body = body;
        this.accessory = accessory;
    }

    @Override
    public int getId() {
        return id.get();
    }
    public int getAccessoryId() {
        return accessory.getId();
    }
    public int getBodyId() {
        return body.getId();
    }
    public int getEngineId() {
        return engine.getId();
    }
}
