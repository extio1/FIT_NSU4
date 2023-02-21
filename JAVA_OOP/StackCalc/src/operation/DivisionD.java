package operation;

import calcException.NotEnoughOperands;
import stack.MyStack;

public class DivisionD implements ArithmeticOperation<Double> {
    @Override
    public void apply(MyStack<Double> stack) throws NotEnoughOperands {
        Double secondOp = stack.pop();
        Double firstOp = stack.pop();
        if(firstOp == null || secondOp == null){
            throw new NotEnoughOperands();
        }
        stack.push(firstOp / secondOp);
    }
}