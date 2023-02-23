package operation;

import calcException.NotEnoughOperandToConfigure;
import calcException.OperationConfigurationError;
import runtimeContext.RuntimeContext;
import stack.MyStack;

public class DefineD implements ContextOperation<Double>, CustomizableOperation{
    private String key;
    private double value;
    @Override
    public void apply(RuntimeContext<Double> context) {
        context.define(key, value);
    }

    @Override
    public void set(String[] option) throws OperationConfigurationError {
        if(option.length != 3){
            throw new NotEnoughOperandToConfigure(this);
        } else {
            key = option[1];
            value = Double.parseDouble(option[2]);
        }
    }
}
