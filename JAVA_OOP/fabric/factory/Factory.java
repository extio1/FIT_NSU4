package factory;

import factory.product.Accessory;
import factory.product.Car;
import factory.product.Engine;
import factory.product.Body;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Factory {
    private int storageBodySize;
    private int storageMotorSize;
    private int storageAccessorySize;
    private int storageAutoSize;

    private int accessorySuppliers;
    private int workers;
    private int dealers;
    private boolean log;

    Storage<Car> carStorage;
    Storage<Body> bodyStorage;
    Storage<Engine> engineStorage;
    Storage<Accessory> accessoryStorage;

    public Factory(){
        readConfigFile("recourses/config.properties");
        carStorage = new SingleSpeciesStorage<>(storageAutoSize);
        bodyStorage = new SingleSpeciesStorage<>(storageBodySize);
        engineStorage = new SingleSpeciesStorage<>(storageMotorSize);
        accessoryStorage = new SingleSpeciesStorage<>(storageAccessorySize);
    }

    private void readConfigFile(String path)  {
        Properties properties = new Properties();

        try(FileInputStream file = new FileInputStream(path)){
            properties.load(file);
            storageBodySize = Integer.parseInt(properties.getProperty("storage_body_size"));
            storageMotorSize = Integer.parseInt(properties.getProperty("storage_motor_size"));
            storageAccessorySize = Integer.parseInt(properties.getProperty("storage_accessory_size"));
            storageAutoSize = Integer.parseInt(properties.getProperty("storage_auto_size"));
            accessorySuppliers = Integer.parseInt(properties.getProperty("accessory_suppliers"));
            workers = Integer.parseInt(properties.getProperty("workers"));
            dealers = Integer.parseInt(properties.getProperty("dealers"));
            log = Boolean.parseBoolean(properties.getProperty("log"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
