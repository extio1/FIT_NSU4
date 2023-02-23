package operation;

import calcException.OperationException;
import calcException.ReferenceToEmptyStack;
import runtimeContext.RuntimeContext;
import runtimeContext.RuntimeContextD;
import stack.MyStack;

public interface StackOperation<T> extends Operation<T> {
    void apply(MyStack<T> stack, RuntimeContext<T> context) throws OperationException, ReferenceToEmptyStack;
}
