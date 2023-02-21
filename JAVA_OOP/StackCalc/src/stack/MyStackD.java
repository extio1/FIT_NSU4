package stack;

import java.util.Stack;

public class MyStackD<T> implements MyStack<T> {
    private Stack<T> data = new Stack<>();

    @Override
    public void push(T value) {
        data.push(value);
    }

    @Override
    public T pop() {
        return data.pop();
    }

    @Override
    public void print() {
        System.out.println(data.peek());
    }
}
