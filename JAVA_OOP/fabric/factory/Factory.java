package factory;

import factory.product.Accessory;
import factory.product.Car;
import factory.product.Engine;
import factory.product.Body;
import factory.storage.SingleSpeciesStorage;
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

    PoolExecutor workers;

    Provider engineProvider;
    Provider bodyProvider;
    Provider[] accessoryProvider;

    public Factory(){
        readConfigFile("recourses/config.properties");
        carStorage = new SingleSpeciesStorage<>(configurationInfo.storageAutoSize());
        bodyStorage = new SingleSpeciesStorage<>(configurationInfo.storageBodySize());
        engineStorage = new SingleSpeciesStorage<>(configurationInfo.storageMotorSize());
        accessoryStorage = new SingleSpeciesStorage<>(configurationInfo.storageAccessorySize());

        workers = new PoolExecutor(configurationInfo.workers());

        engineProvider = new Provider(configurationInfo.engineDelay(), Engine.class, engineStorage);
        bodyProvider = new Provider(configurationInfo.bodyDelay(), Body.class, bodyStorage);
        accessoryProvider = new Provider[configurationInfo.accessorySuppliers()];
        for(int i = 0; i < configurationInfo.accessorySuppliers(); ++i)
            accessoryProvider[i] = new Provider(configurationInfo.accessorySuppliers(), Accessory.class, accessoryStorage);
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
            int engineDelay = Integer.parseInt(properties.getProperty("engine_dealer_default_delay"));
            int accessoryDelay = Integer.parseInt(properties.getProperty("accessory_dealer_default_delay"));
            int bodyDelay = Integer.parseInt(properties.getProperty("body_dealer_default_delay"));
            boolean log = Boolean.parseBoolean(properties.getProperty("log"));

            configurationInfo = new ConfigurationInfo(
                    storageBodySize, storageMotorSize, storageAccessorySize, storageAutoSize,
                    accessorySuppliers, workers, dealers,
                    engineDelay, accessoryDelay, bodyDelay,
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
            boolean log
    ){}
}
