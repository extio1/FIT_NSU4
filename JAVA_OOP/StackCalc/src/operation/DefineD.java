package operation;

import calcException.NotEnoughOperandToConfigure;
import calcException.OperationConfigurationError;
import main.Main;
import runtimeContext.RuntimeContext;

import java.util.logging.Level;

public class DefineD implements ContextOperation<Double>, CustomizableOperation{
    private String key;
    private double value;
    @Override
    public void apply(RuntimeContext<Double> context) {
        context.define(key, value);
        Main.logger.log(Level.INFO, this.toString()+" defined "+key+" as "+value);
    }

    @Override
    public void set(String[] option) throws OperationConfigurationError {
        if(option.length != 3){
            throw new NotEnoughOperandToConfigure(this);
        } else {
            key = option[1];
            value = Double.parseDouble(option[2]);
            Main.logger.log(Level.INFO, this.toString()+" is configured by "+key+" - "+value);
        }
    }
}
