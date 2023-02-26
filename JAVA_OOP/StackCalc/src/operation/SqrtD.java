package operation;

import calcException.OperationNotEnoughOperands;
import calcException.ReferenceToEmptyStack;
import main.Main;
import stack.MyStack;

import java.util.logging.Level;


public class SqrtD implements ArithmeticOperation<Double> {
    @Override
    public void apply(MyStack<Double> stack) throws OperationNotEnoughOperands {
        try {
            double argument = stack.pop();
            double result = Math.sqrt(argument);
            Main.logger.log(Level.INFO, "Result: "+result);
            stack.push(result);
        } catch (ReferenceToEmptyStack e){
            throw new OperationNotEnoughOperands(this);
        }
    }
}
