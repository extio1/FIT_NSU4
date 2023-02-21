package operation;

import runtimeContext.RuntimeContext;
import stack.MyStack;


public class SqrtD implements ArithmeticOperation<Double> {
    @Override
    public void apply(MyStack<Double> stack) {
        double argument = stack.pop();
        stack.push(Math.sqrt(argument));
    }
}