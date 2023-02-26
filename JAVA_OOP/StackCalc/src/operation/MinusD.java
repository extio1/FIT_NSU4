package operation;

import calcException.OperationNotEnoughOperands;
import calcException.ReferenceToEmptyStack;
import main.Main;
import stack.MyStack;

import java.util.logging.Level;

public class MinusD implements ArithmeticOperation<Double> {
    @Override
    public void apply(MyStack<Double> stack) throws OperationNotEnoughOperands{
        try {
            double secondOp = stack.pop();
            double firstOp = stack.pop();
            double result = firstOp - secondOp;
            Main.logger.log(Level.INFO, "Result: "+result);
            stack.push(result);
        } catch (ReferenceToEmptyStack e){
            throw new OperationNotEnoughOperands(this);
        }
    }
}
