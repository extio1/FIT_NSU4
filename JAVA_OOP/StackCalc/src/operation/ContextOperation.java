package operation;

import calcException.NotEnoughOperands;
import runtimeContext.RuntimeContext;
import stack.MyStack;

public interface ContextOperation<T> extends Operation<T> {
    void apply(RuntimeContext<T> context) throws NotEnoughOperands;
}
