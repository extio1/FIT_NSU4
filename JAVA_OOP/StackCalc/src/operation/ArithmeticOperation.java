package operation;

import calcException.OperationException;
import stack.MyStack;

public interface ArithmeticOperation<T> extends Operation<T> {
    void apply(MyStack<T> stack) throws OperationException;
}
