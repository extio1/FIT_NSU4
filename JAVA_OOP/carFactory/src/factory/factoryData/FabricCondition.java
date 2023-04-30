package factory.factoryData;

import factory.Factory;
import factory.Package;

import java.util.concurrent.atomic.AtomicInteger;

public class FabricCondition implements Package {
    private final int bodyStorageCapacity;
    private final int engineStorageCapacity;
    private final int accessoryStorageCapacity;
    private final int carStorageCapacity;

    private final AtomicInteger bodyCurrCapacity = new AtomicInteger(0);
    private final AtomicInteger engineCurrCapacity = new AtomicInteger(0);
    private final AtomicInteger accessoryCurrCapacity = new AtomicInteger(0);
    private final AtomicInteger carCurrCapacity = new AtomicInteger(0);
    private final AtomicInteger carInQueue = new AtomicInteger(0);

    private int bodyProductionAmount = 0;
    private int engineProductionAmount = 0;
    private final AtomicInteger accessoryProductionAmount = new AtomicInteger(0);
    private final AtomicInteger carProductionAmount = new AtomicInteger(0);

    public FabricCondition(Factory factory){
        bodyStorageCapacity = factory.getFabricInfo().storageBodySize();
        engineStorageCapacity = factory.getFabricInfo().storageMotorSize();
        accessoryStorageCapacity = factory.getFabricInfo().storageAccessorySize();
        carStorageCapacity = factory.getFabricInfo().storageAutoSize();
    }

    public void incrementAccessoryCurrCapacity() {
        accessoryCurrCapacity.incrementAndGet();
    }
    public void decrementAccessoryCurrCapacity() {
        accessoryCurrCapacity.decrementAndGet();
    }
    public void incrementCarCurrCapacity() {
        carCurrCapacity.incrementAndGet();
    }
    public void decrementCarCurrCapacity() {
        carCurrCapacity.decrementAndGet();
    }
    public void incrementBodyCurrCapacity() {
        bodyCurrCapacity.incrementAndGet();
    }
    public void decrementBodyCurrCapacity() {
        bodyCurrCapacity.decrementAndGet();
    }
    public void incrementEngineCurrCapacity() {
        engineCurrCapacity.incrementAndGet();
    }
    public void decrementEngineCurrCapacity() {
        engineCurrCapacity.decrementAndGet();
    }
    public int getAccessoryCurrCapacity() {
        return accessoryCurrCapacity.get();
    }
    public int getCarCurrCapacity() {
        return carCurrCapacity.get();
    }
    public int getEngineCurrCapacity() {
        return engineCurrCapacity.get();
    }
    public int getBodyCurrCapacity() {
        return bodyCurrCapacity.get();
    }
    public void incrementCarsInQueue() {
        carInQueue.incrementAndGet();
    }
    public void decrementCarsInQueue() {
        carInQueue.decrementAndGet();;
    }

    public int getCarInQueue() {
        return carInQueue.get();
    }

    public void incrementAccessoryProductionAmount() {
        accessoryProductionAmount.incrementAndGet();
    }

    public void incrementBodyProductionAmount() {
        ++bodyProductionAmount;
    }

    public void incrementCarProductionAmount() {
        carProductionAmount.incrementAndGet();
    }

    public void incrementEngineProductionAmount() {
        ++engineProductionAmount;
    }

    public int getAccessoryProductionAmount() {
        return accessoryProductionAmount.get();
    }

    public int getAccessoryStorageCapacity() {
        return accessoryStorageCapacity;
    }

    public int getBodyProductionAmount() {
        return bodyProductionAmount;
    }

    public int getBodyStorageCapacity() {
        return bodyStorageCapacity;
    }

    public int getCarProductionAmount() {
        return carProductionAmount.get();
    }

    public int getCarStorageCapacity() {
        return carStorageCapacity;
    }

    public int getEngineProductionAmount() {
        return engineProductionAmount;
    }

    public int getEngineStorageCapacity() {
        return engineStorageCapacity;
    }
}
