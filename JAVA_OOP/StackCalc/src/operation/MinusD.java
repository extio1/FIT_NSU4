package operation;

import calcException.OperationNotEnoughOperands;
import calcException.ReferenceToEmptyStack;
import stack.MyStack;

public class MinusD implements ArithmeticOperation<Double> {
    @Override
    public void apply(MyStack<Double> stack) throws OperationNotEnoughOperands{
        try {
            double secondOp = stack.pop();
            double firstOp = stack.pop();
            stack.push(firstOp - secondOp);
        } catch (ReferenceToEmptyStack e){
            throw new OperationNotEnoughOperands(this);
        }
    }
}
