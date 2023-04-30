package factory.provision;

import factory.*;

import java.util.ArrayList;
import java.util.List;

public class ProviderSet <T extends Product> implements InteractivePerformerSet {
    private final List<Provider<T>> providerList;

    public ProviderSet(int nProviders, int period, Class<? extends Product> providingProduct, Storage<T> storage){
        providerList = new ArrayList<>();
        for(int i = 0; i < nProviders; ++i) {
            providerList.add(new Provider<T>(period, providingProduct, storage, i));
        }
    }

    @Override
    public void startPerformSet(){
        providerList.forEach(Provider::startPerform);
    }

    @Override
    public void stopPerformSet(){
        providerList.forEach(Provider::stopPerform);
    }

    @Override
    public void changePeriodSet(int newPeriod) {
        providerList.forEach((e)->e.changePeriod(newPeriod));
    }

}