package operation;

import runtimeContext.RuntimeContext;
import stack.MyStack;

public interface StackOperation<T> extends Operation<T> {
    void apply(MyStack<T> stack, RuntimeContext<T> context);
}
