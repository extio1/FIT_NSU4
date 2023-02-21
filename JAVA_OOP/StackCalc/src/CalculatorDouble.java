import calcException.NotEnoughOperands;
import operation.ArithmeticOperation;
import operation.ContextOperation;
import operation.Operation;
import operation.StackOperation;
import runtimeContext.RuntimeContext;
import runtimeContext.RuntimeContextD;
import stack.MyStack;

public class CalculatorDouble implements Calculator<Double> {
    private final MyStack<Double> stack = new MyStack<>();
    private final RuntimeContextD context = new RuntimeContextD();

    @Override
    public void execute(Operation<Double> operation) throws NotEnoughOperands {
        if(operation instanceof ArithmeticOperation){
            operation.apply(stack);
        }
        else if (operation instanceof StackOperation){
            operation.apply(stack, context);
        }
        else if (operation instanceof ContextOperation){
            operation.apply(context);
        }
    }
}
