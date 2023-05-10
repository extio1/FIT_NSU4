package factory.carStorageController;

import factory.Storage;
import factory.product.Accessory;
import factory.product.Body;
import factory.product.Car;
import factory.job.CreateCar;
import factory.product.Engine;
import threadpool.PoolExecutor;

public class CarStorageController extends Thread {
    private final Storage<Car> carStorage;
    private final Storage<Body> bodyStorage;
    private final Storage<Engine> engineStorage;
    private final Storage<Accessory> accessoryStorage;
    private final PoolExecutor workers;

    public CarStorageController(Storage<Car> carStorage, Storage<Engine> engineStorage, Storage<Body> bodyStorage,
                                Storage<Accessory> accessoryStorage, PoolExecutor workers){
        this.carStorage = carStorage;
        this.bodyStorage = bodyStorage;
        this.engineStorage = engineStorage;
        this.accessoryStorage = accessoryStorage;
        this.workers = workers;
        this.setName("Car storage controller thread");
        for(int i = 0; i < carStorage.getCapacity(); ++i){
            workers.execute(new CreateCar(engineStorage, bodyStorage, accessoryStorage, carStorage));
        }
    }

    @Override
    public void run(){
        synchronized (carStorage) {
            while(!Thread.interrupted()){
                try {
                    carStorage.wait();
                } catch (InterruptedException e) {
                    break;
                }
                workers.execute(new CreateCar(engineStorage, bodyStorage, accessoryStorage, carStorage));
            }
        }
    }
}
