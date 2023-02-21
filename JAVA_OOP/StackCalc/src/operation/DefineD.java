package operation;

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
    public void set(String[] option) throws Exception {
        if(option.length != 3){
            throw new Exception();
        } else {
            key = option[1];
            value = Double.parseDouble(option[2]);
        }
    }
}
