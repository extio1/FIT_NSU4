package factory.carStorageController;

import factory.Storage;
import factory.product.Car;
import factory.job.CreateCar;
import threadpool.PoolExecutor;

public class CarStorageController extends Thread {
    private final Storage<Car> storage;
    private final PoolExecutor workers;

    public CarStorageController(Storage<Car> storage, PoolExecutor workers){
        this.storage = storage;
        this.workers = workers;
        this.setName("Car storage controller thread");
        for(int i = 0; i < storage.getCapacity(); ++i){
            workers.execute(new CreateCar());
        }
    }

    @Override
    public void run(){
        while(!Thread.interrupted()){
            try {
                synchronized (storage) {
                    storage.wait();
                }
            } catch (InterruptedException e) {
                break;
            }
            System.out.println("New car creation");
            workers.execute(new CreateCar());
        }
    }

}
