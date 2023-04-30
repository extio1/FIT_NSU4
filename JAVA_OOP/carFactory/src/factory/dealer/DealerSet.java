package factory.dealer;

import factory.InteractivePerformerSet;
import factory.Storage;
import factory.provision.Provider;
import factory.product.Car;

import java.util.ArrayList;

public class DealerSet implements InteractivePerformerSet {
    private final ArrayList<Dealer> dealerList;

    public DealerSet(int nDealers, int period, Storage<Car> storage){
        dealerList = new ArrayList<>();
        for(int i = 0; i < nDealers; ++i)
            dealerList.add(new Dealer(period, storage, i));

    }

    @Override
    public void startPerformSet(){
        dealerList.forEach(Dealer::startPerform);
    }

    @Override
    public void stopPerformSet(){
        dealerList.forEach(Dealer::stopPerform);
    }

    @Override
    public void changePeriodSet(int newPeriod) {
        dealerList.forEach((e)->e.changePeriod(newPeriod));
    }
}
