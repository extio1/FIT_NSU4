package factory.product;

import factory.Product;

public class Car extends Product {
    public Car(int id, Engine engine, Body body, Accessory accessory) {
        super(id);
    }
}
