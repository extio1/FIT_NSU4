
import calcException.OperationException;
import calcException.OperationNotEnoughOperands;
import calcException.ReferenceToEmptyStack;
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
    public void execute(Operation<Double> operation) {
        if(operation instanceof ArithmeticOperation){
            try {
                operation.apply(stack);
            } catch (OperationException e){
                System.out.println(e.getMessage());
            }
        }
        else if (operation instanceof StackOperation){
            try {
                operation.apply(stack, context);
            } catch (ReferenceToEmptyStack e) {
                System.out.println(e.getMessage());
            } catch (OperationException e) {
                System.out.println(e.getMessage());
            }
        }
        else if (operation instanceof ContextOperation){
            try {
                operation.apply(context);
            } catch (OperationException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
