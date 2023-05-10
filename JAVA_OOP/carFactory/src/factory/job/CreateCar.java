package factory.job;

import factory.Storage;
import factory.product.Accessory;
import factory.product.Body;
import factory.product.Car;
import factory.product.Engine;

public class CreateCar implements Runnable{
    private final Storage<Engine> engineStorage;
    private final Storage<Body> bodyStorage;
    private final Storage<Accessory> accessoryStorage;
    private final Storage<Car> carStorage;

    public CreateCar(Storage<Engine> engineStorage, Storage<Body> bodyStorage,
                     Storage<Accessory> accessoryStorage, Storage<Car> carStorage){
        this.engineStorage = engineStorage;
        this.bodyStorage = bodyStorage;
        this.accessoryStorage = accessoryStorage;
        this.carStorage = carStorage;
    }

    @Override
    public void run(){
        Body body = bodyStorage.getComponent();
        Engine engine = engineStorage.getComponent();
        Accessory accessory = accessoryStorage.getComponent();

        Car car = new Car(engine, body, accessory);
        carStorage.pushComponent(car);
    }
}
