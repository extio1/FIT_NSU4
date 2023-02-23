package operation;

import calcException.OperationNotEnoughOperands;
import calcException.ReferenceToEmptyStack;
import stack.MyStack;


public class SqrtD implements ArithmeticOperation<Double> {
    @Override
    public void apply(MyStack<Double> stack) throws OperationNotEnoughOperands {
        try {
            double argument = stack.pop();
            stack.push(Math.sqrt(argument));
        } catch (ReferenceToEmptyStack e){
            throw new OperationNotEnoughOperands(this);
        }


    }
}