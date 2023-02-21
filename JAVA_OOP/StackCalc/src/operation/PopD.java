package operation;

import stack.MyStack;

public class PopD implements ArithmeticOperation<Double>{
    @Override
    public void apply(MyStack<Double> stack) {
        stack.pop();
    }
}
