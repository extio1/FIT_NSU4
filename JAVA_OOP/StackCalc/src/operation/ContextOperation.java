package operation;

import runtimeContext.RuntimeContext;
import stack.MyStack;

public interface ContextOperation<T> extends Operation<T> {
    void apply(MyStack<T> stack, RuntimeContext<T> context);
    void set(String[] option);
}
