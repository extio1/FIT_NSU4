package operation;

import runtimeContext.RuntimeContext;
import runtimeContext.RuntimeContextD;
import stack.MyStack;

public class PrintD implements StackOperation<Double>{
    @Override
    public void apply(MyStack<Double> stack, RuntimeContext<Double> context) {
        stack.print();
    }

}
