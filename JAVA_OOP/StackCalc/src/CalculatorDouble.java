import operation.ArithmeticOperation;
import operation.ContextOperation;
import operation.Operation;
import operation.StackOperation;
import runtimeContext.RuntimeContext;
import runtimeContext.RuntimeContextD;
import stack.MyStackD;

public class CalculatorDouble implements Calculator<Double> {
    private final MyStackD stack = new MyStackD();
    private final RuntimeContextD context = new RuntimeContextD();

    @Override
    public void execute(Operation<Double> operation){
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
