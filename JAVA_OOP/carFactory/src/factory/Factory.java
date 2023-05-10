package factory;

import factory.carStorageController.CarStorageController;
import factory.dealer.DealerSet;
import factory.factoryData.FabricCondition;
import factory.product.Accessory;
import factory.product.Car;
import factory.product.Engine;
import factory.product.Body;
import factory.provision.Provider;
import factory.provision.ProviderSet;
import factory.storage.SingleSpeciesStorage;
import factory.job.CreateCar;
import gui.Observer;
import threadpool.PoolExecutor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;


public class Factory implements Publisher{
    private volatile boolean closedOnce = false;
    private final List<Observer> observers = new ArrayList<>();

    public static volatile FabricCondition fabricConditionDescriptor;

    public static Logger logger = null;

    private ConfigurationInfo configurationInfo;

    private final Storage<Car> carStorage;
    private final Storage<Body> bodyStorage;
    private final Storage<Engine> engineStorage;
    private final Storage<Accessory> accessoryStorage;

    private final PoolExecutor workers;

    private final CarStorageController carStorageController;

    private final Provider<Engine> engineProvider;
    private final Provider<Body> bodyProvider;
    private final ProviderSet<Accessory> accessoryProvider;

    private final DealerSet dealerSet;

    private static final int SECOND_IN_MS = 1000;
    private static final int FPS = 30;
    private final Thread observerUpdater = new Thread(new Runnable() {
        @Override
        public void run() {
            while(!Thread.interrupted()){
                try {
                    Thread.sleep(SECOND_IN_MS/FPS);
                    signalizeAll();
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    });


    public Factory(){
        this("recourses/config.properties");
    }

    public Factory(String configPath) {
        readConfigFile(configPath);
        fabricConditionDescriptor = new FabricCondition(this);

        //Storages creating
        carStorage = new SingleSpeciesStorage<>(configurationInfo.storageAutoSize());
        bodyStorage = new SingleSpeciesStorage<>(configurationInfo.storageBodySize());
        engineStorage = new SingleSpeciesStorage<>(configurationInfo.storageMotorSize());
        accessoryStorage = new SingleSpeciesStorage<>(configurationInfo.storageAccessorySize());

        //Thread pool creating
        workers = new PoolExecutor(configurationInfo.workers());

        //Cars storage controller
        carStorageController = new CarStorageController(carStorage, engineStorage, bodyStorage, accessoryStorage, workers);

        //Providers creating
        engineProvider = new Provider<>(configurationInfo.engineDelay(), Engine.class, engineStorage, 0);
        bodyProvider = new Provider<>(configurationInfo.bodyDelay(), Body.class, bodyStorage, 0);
        accessoryProvider = new ProviderSet<>(configurationInfo.accessorySuppliers(), configurationInfo.accessoryDelay(),
                                               Accessory.class, accessoryStorage);

        //Dealers creating
        dealerSet = new DealerSet(configurationInfo.dealers(), configurationInfo.dealerDelay, carStorage);

        //Enabling logging if demands
        if(configurationInfo.log()){
            logger = Logger.getLogger("FactoryDebug.log");
            try {
                LogManager.getLogManager().readConfiguration(new FileInputStream("recourses/logging.properties"));
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public void startFabric(){
        if(!closedOnce) {
            engineProvider.startPerform();
            bodyProvider.startPerform();
            accessoryProvider.startPerformSet();
            dealerSet.startPerformSet();
            carStorageController.start();
            observerUpdater.start();
        }
    }

    public void closeFabric(){
        logger.log(Level.INFO, "FABRIC CLOSED");
        carStorageController.interrupt();
        observerUpdater.interrupt();
        dealerSet.stopPerformSet();
        accessoryProvider.stopPerformSet();
        engineProvider.stopPerform();
        bodyProvider.stopPerform();
        workers.quit();
        closedOnce = true;
    }

    public record ConfigurationInfo(
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

    public InteractivePerformer getEngineProvider(){
        return engineProvider;
    }
    public InteractivePerformer getBodyProvider(){
        return bodyProvider;
    }
    public InteractivePerformerSet getAccessoryProvider(){
        return accessoryProvider;
    }
    public InteractivePerformerSet getDealers(){
        return dealerSet;
    }

    public ConfigurationInfo getFabricInfo(){
        return configurationInfo;
    }

    @Override
    public void attach(Observer obs) {
        observers.add(obs);
    }

    @Override
    public void detach(Observer obs) {
        observers.remove(obs);
    }

    @Override
    public void signalizeAll() {
        observers.forEach(Observer::update);
    }

    @Override
    public Package getInfo() {
        return fabricConditionDescriptor;
    }

}
