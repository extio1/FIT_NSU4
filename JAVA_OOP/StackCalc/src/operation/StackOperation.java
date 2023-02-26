package operation;

import calcException.OperationException;
import calcException.ReferenceToEmptyStack;
import calcException.WrongFormatInputData;
import runtimeContext.RuntimeContext;
import stack.MyStack;

public interface StackOperation<T> extends Operation<T> {
    void apply(MyStack<T> stack, RuntimeContext<T> context) throws OperationException, ReferenceToEmptyStack, WrongFormatInputData;
}
