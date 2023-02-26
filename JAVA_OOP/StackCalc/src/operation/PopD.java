package operation;

import calcException.ReferenceToEmptyStack;
import main.Main;
import runtimeContext.RuntimeContext;
import stack.MyStack;

import java.util.logging.Level;

public class PopD implements StackOperation<Double>{
    @Override
    public void apply(MyStack<Double> stack, RuntimeContext<Double> context) throws ReferenceToEmptyStack {
        stack.print();
        Main.logger.log(Level.INFO, "Pop'ed: "+stack.pop());
    }
}
