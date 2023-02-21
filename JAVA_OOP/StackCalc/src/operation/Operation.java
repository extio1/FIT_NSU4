package operation;

import runtimeContext.RuntimeContext;
import stack.MyStack;

public interface Operation<T> {
    default void apply(MyStack<T> stack) {}
    default void apply(MyStack<T> stack, RuntimeContext<T> context) {}
    default void apply(RuntimeContext<T> context) {}
}
