package controller;

import factory.Factory;
import factory.InteractivePerformer;
import factory.InteractivePerformerSet;

public class FabricController {
    private final InteractivePerformer bodyProvider;
    private final InteractivePerformer engineProvider;
    private final InteractivePerformerSet accessoryProvider;
    private final InteractivePerformerSet dealerSet;

    private final Factory fabric;

    public FabricController(Factory fabric)
    {
        this.fabric = fabric;
        this.bodyProvider = fabric.getBodyProvider();
        this.engineProvider = fabric.getEngineProvider();
        this.accessoryProvider = fabric.getAccessoryProvider();
        this.dealerSet = fabric.getDealers();
    }

    public void changePeriodPerformer(Performer performer, int newPeriod){
        switch (performer){
            case dealer -> dealerSet.changePeriodSet(newPeriod);
            case bodyProv -> bodyProvider.changePeriod(newPeriod);
            case accessoryProv -> accessoryProvider.changePeriodSet(newPeriod);
            case engineProv -> engineProvider.changePeriod(newPeriod);
        }
    }

    public void startFabric(){
        fabric.startFabric();
    }

    public void closeFabric(){
        fabric.closeFabric();
    }
}
