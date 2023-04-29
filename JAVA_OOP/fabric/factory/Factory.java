package factory;

import factory.carStorageController.CarStorageController;
import factory.dealer.DealerSet;
import factory.product.Accessory;
import factory.product.Car;
import factory.product.Engine;
import factory.product.Body;
import factory.provision.Provider;
import factory.provision.ProviderSet;
import factory.storage.SingleSpeciesStorage;
import factory.job.CreateCar;
import threadpool.PoolExecutor;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Factory {
    ConfigurationInfo configurationInfo;

    Storage<Car> carStorage;
    Storage<Body> bodyStorage;
    Storage<Engine> engineStorage;
    Storage<Accessory> accessoryStorage;

    CarStorageController carStorageController;

    PoolExecutor workers;

    Provider<Engine> engineProvider;
    Provider<Body> bodyProvider;
    ProviderSet<Accessory> accessoryProvider;

    DealerSet dealerSet;

    public Factory(){
        this("recourses/config.properties");
    }

    public Factory(String configPath){
        readConfigFile(configPath);

        carStorage = new SingleSpeciesStorage<>(configurationInfo.storageAutoSize());
        bodyStorage = new SingleSpeciesStorage<>(configurationInfo.storageBodySize());
        engineStorage = new SingleSpeciesStorage<>(configurationInfo.storageMotorSize());
        accessoryStorage = new SingleSpeciesStorage<>(configurationInfo.storageAccessorySize());
        CreateCar.setStorages(engineStorage, bodyStorage, accessoryStorage, carStorage);

        workers = new PoolExecutor(configurationInfo.workers());

        carStorageController = new CarStorageController(carStorage, workers);

        engineProvider = new Provider<>(configurationInfo.engineDelay(), Engine.class, engineStorage, 0);
        bodyProvider = new Provider<>(configurationInfo.bodyDelay(), Body.class, bodyStorage, 0);
        accessoryProvider = new ProviderSet<>(configurationInfo.accessorySuppliers(), configurationInfo.accessoryDelay(),
                                               Accessory.class, accessoryStorage);

        dealerSet = new DealerSet(configurationInfo.dealers(), configurationInfo.dealerDelay, carStorage);
    }

    public void startFabric(){
        engineProvider.startPerform();
        bodyProvider.startPerform();
        accessoryProvider.startPerformSet();
        dealerSet.startPerformSet();
        carStorageController.start();
    }

    public void closeFabric(){
        engineProvider.stopPerform();
        bodyProvider.stopPerform();
        accessoryProvider.stopPerformSet();
        dealerSet.stopPerformSet();
        carStorageController.interrupt();
    }

    private void readConfigFile(String path)  {
        Properties properties = new Properties();

        try(FileInputStream file = new FileInputStream(path)){
            properties.load(file);
            int storageBodySize = Integer.parseInt(properties.getProperty("storage_body_size"));
            int storageMotorSize = Integer.parseInt(properties.getProperty("storage_motor_size"));
            int storageAccessorySize = Integer.parseInt(properties.getProperty("storage_accessory_size"));
            int storageAutoSize = Integer.parseInt(properties.getProperty("storage_auto_size"));
            int accessorySuppliers = Integer.parseInt(properties.getProperty("n_accessory_suppliers"));
            int workers = Integer.parseInt(properties.getProperty("n_workers"));
            int dealers = Integer.parseInt(properties.getProperty("n_dealers"));
            int engineDelay = Integer.parseInt(properties.getProperty("engine_provider_default_delay"));
            int accessoryDelay = Integer.parseInt(properties.getProperty("accessory_provider_default_delay"));
            int bodyDelay = Integer.parseInt(properties.getProperty("body_provider_default_delay"));
            int dealerDelay = Integer.parseInt(properties.getProperty("dealer_default_delay"));
            boolean log = Boolean.parseBoolean(properties.getProperty("log"));

            configurationInfo = new ConfigurationInfo(
                    storageBodySize, storageMotorSize, storageAccessorySize, storageAutoSize,
                    accessorySuppliers, workers, dealers,
                    engineDelay, accessoryDelay, bodyDelay, dealerDelay,
                    log
            );

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private record ConfigurationInfo(
            int storageBodySize,
            int storageMotorSize,
            int storageAccessorySize,
            int storageAutoSize,

            int accessorySuppliers,
            int workers,
            int dealers,

            int engineDelay,
            int accessoryDelay,
            int bodyDelay,
            int dealerDelay,

            boolean log
    ){}
}
