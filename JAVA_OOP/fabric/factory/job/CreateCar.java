package factory.job;

import factory.Storage;
import factory.product.Accessory;
import factory.product.Body;
import factory.product.Car;
import factory.product.Engine;

public class CreateCar implements Runnable{
    private static Storage<Engine> engineStorage;
    private static Storage<Body> bodyStorage;
    private static Storage<Accessory> accessoryStorage;
    private static Storage<Car> carStorage;

    private static int vin = 1;

    public CreateCar(){
        ++vin;
    }

    public static void setStorages(Storage<Engine> engineStorage, Storage<Body> bodyStorage,
                            Storage<Accessory> accessoryStorage, Storage<Car> carStorage) {
        CreateCar.engineStorage = engineStorage;
        CreateCar.carStorage = carStorage;
        CreateCar.bodyStorage = bodyStorage;
        CreateCar.accessoryStorage = accessoryStorage;
    }

    @Override
    public void run(){
        Body body = bodyStorage.getComponent();
        Engine engine = engineStorage.getComponent();
        Accessory accessory = accessoryStorage.getComponent();

        Car car = new Car(vin, engine, body, accessory);
        carStorage.pushComponent(car);
    }
}
