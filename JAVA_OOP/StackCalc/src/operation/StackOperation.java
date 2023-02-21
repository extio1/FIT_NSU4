package operation;

import stack.MyStack;

public interface StackOperation<T> {
    void apply(MyStack<T> stack);
}
