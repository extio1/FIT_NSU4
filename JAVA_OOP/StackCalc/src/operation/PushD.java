package operation;

import calcException.NotEnoughOperandToConfigure;
import runtimeContext.RuntimeContext;
import stack.MyStack;

public class PushD implements StackOperation<Double>, CustomizableOperation {
    private String arg;

    @Override
    public void apply(MyStack<Double> stack, RuntimeContext<Double> context) {
        stack.push(context.getIfDefined(arg));
    }

    @Override
    public void set(String[] option) throws NotEnoughOperandToConfigure {
        if(option.length != 2){
            throw new NotEnoughOperandToConfigure(this);
        } else {
            arg = option[1];
        }
    }
}
