package operation;

import stack.MyStack;

public class PlusD implements ArithmeticOperation<Double>{
    @Override
    public void apply(MyStack<Double> stack) {
        System.out.println("PlusD\n");
    }
}
