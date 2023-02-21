import operation.ArithmeticOperation;
import operation.ContextOperation;
import operation.Operation;
import operation.StackOperation;
import runtimeContext.RuntimeContext;
import runtimeContext.RuntimeContextD;
import stack.MyStackD;

public class CalculatorDouble implements Calculator {
    private final MyStackD<Double> stack = new MyStackD<>();
    private final RuntimeContextD context = new RuntimeContextD();

    @Override
    public void execute(Operation operation){
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
