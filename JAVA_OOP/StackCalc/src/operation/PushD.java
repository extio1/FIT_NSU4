package operation;

import runtimeContext.RuntimeContext;
import stack.MyStack;

public class PushD implements Operation<Double>, CustomizableOperation {
    private String arg;

    @Override
    public void apply(MyStack<Double> stack, RuntimeContext<Double> context) {
        stack.push(context.getIfDefined(arg));
    }

    @Override
    public void set(String[] option) throws Exception {
        if(option.length != 2){
            throw new Exception();
        } else {
            arg = option[1];
        }
    }
}
