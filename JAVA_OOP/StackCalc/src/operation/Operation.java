package operation;

import calcException.NotEnoughOperands;
import runtimeContext.RuntimeContext;
import stack.MyStack;

public interface Operation<T> {
    default void apply(MyStack<T> stack) throws NotEnoughOperands {}
    default void apply(MyStack<T> stack, RuntimeContext<T> context) throws NotEnoughOperands {}
    default void apply(RuntimeContext<T> context) throws NotEnoughOperands {}
}
