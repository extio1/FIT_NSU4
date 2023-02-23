package operation;

import calcException.OperationNotEnoughOperands;
import calcException.OperationZeroDivision;
import calcException.ReferenceToEmptyStack;
import stack.MyStack;

public class DivisionD implements ArithmeticOperation<Double> {
    @Override
    public void apply(MyStack<Double> stack) throws OperationNotEnoughOperands, OperationZeroDivision {
        try {
            double secondOp = stack.pop();
            double firstOp = stack.pop();
            if(secondOp == 0){
                throw new OperationZeroDivision(this);
            }
            stack.push(firstOp / secondOp);
        } catch (ReferenceToEmptyStack e){
            throw new OperationNotEnoughOperands(this);
        }
    }
}