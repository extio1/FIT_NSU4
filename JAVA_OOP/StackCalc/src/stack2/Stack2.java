package stack2;

import java.util.Stack;

public class Stack2<T>{
    private Stack<T> data = new Stack<T>();

    void push(T value){
        data.push(value);
    }
    T pop(){
        return data.pop();
    }
    void print(){
        System.out.println(data.peek());
    }

}
