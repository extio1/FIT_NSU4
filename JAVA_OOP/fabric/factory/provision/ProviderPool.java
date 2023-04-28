package factory.provision;

import factory.Product;
import factory.Storage;
import factory.product.Accessory;

import java.util.ArrayList;
import java.util.Iterator;

public class ProviderPool <T extends Product>{
    private final ArrayList<Provider<T>> providerList;

    public ProviderPool(int nProviders, int period, Class<? extends Product> providingProduct, Storage<T> storage){
        providerList = new ArrayList<>(nProviders);
        for(int i = 0; i < nProviders; ++i)
            providerList.set(i, new Provider<T>(period, providingProduct, storage));
    }

    public void startSupplies(){
        providerList.forEach(Provider::startSupplies);
    }

    public void stopSupplies(){
        providerList.forEach(Provider::stopSupplies);
    }

    public void changePeriod(int newPeriod) {
        providerList.forEach((e)->e.changePeriod(newPeriod));
    }
}
