package operation;

import stack.MyStack;

public class MinusD implements ArithmeticOperation<Double> {
    @Override
    public void apply(MyStack<Double> stack) {
        double secondOp = stack.pop();
        double firstOp = stack.pop();
        stack.push(firstOp - secondOp);
    }
}
