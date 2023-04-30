package factory.dealer;

import factory.InteractivePerformer;
import factory.Storage;
import factory.product.Car;

import java.util.logging.Level;

import static factory.Factory.logger;

public class Dealer implements InteractivePerformer {
    private volatile int period; //period in ms
    private int dealerIndexNumber;
    Thread executor;

    Storage<Car> storage;

    public Dealer(int period, Storage<Car> storage, int dealerIndexNumber){
        this.period = period;
        this.storage = storage;
        executor = new Thread(new Dealer.Task());
        this.dealerIndexNumber = dealerIndexNumber;
        executor.setName("Dealer thread "+dealerIndexNumber);
    }

    public Dealer(int period, Storage<Car> storage){
        this(period, storage, 0);
    }

    @Override
    public void startPerform(){
        executor.start();
    }

    @Override
    public void stopPerform(){
        executor.interrupt();
    }

    @Override
    public void changePeriod(int newPeriod) {
        period = newPeriod;
    }

    private class Task implements Runnable{
        @Override
        public void run() {
            while(!Thread.interrupted()) {
                try {
                    Thread.sleep(period);
                    doTask();
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        private void doTask() {
            Car car = storage.getComponent();
            if (car != null) {
                logger.log(Level.INFO, "New car was bought. Dealer " + dealerIndexNumber +
                        ": Auto" + car.getId() + "(Body:" + car.getBodyId() + ", Motor:" + car.getBodyId() +
                        ", Accessory:" + car.getAccessoryId() + ")");
            }
        }
    }
}
