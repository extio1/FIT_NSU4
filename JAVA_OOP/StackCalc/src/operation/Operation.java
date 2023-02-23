package operation;

import calcException.OperationException;
import calcException.ReferenceToEmptyStack;
import runtimeContext.RuntimeContext;
import stack.MyStack;

public interface Operation<T> {
    default void apply(MyStack<T> stack) throws OperationException {}
    default void apply(MyStack<T> stack, RuntimeContext<T> context) throws OperationException, ReferenceToEmptyStack {}
    default void apply(RuntimeContext<T> context) throws OperationException {}
}
