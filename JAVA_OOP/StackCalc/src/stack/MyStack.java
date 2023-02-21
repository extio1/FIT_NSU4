package stack;

import java.util.Stack;

public class MyStack<T> {
    private Stack<T> data = new Stack<>();
    public void push(T value){
        data.push(value);
    }
    public T pop(){
        return data.pop();
    }
    public void print(){
        System.out.println(data.peek());
    }

}
