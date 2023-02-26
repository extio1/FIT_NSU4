package stack;

import calcException.ReferenceToEmptyStack;
import calcException.WrongFormatInputData;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class MyStackTest {
    private final MyStack<Double> stack = new MyStack<>();
    double[] testArr = {0, 5, 250.333, -250.333, 123.123123123123, -2147483648, 2147483647, 4.9*Math.pow(10, -324),
            4.9*Math.pow(10, -324),  1.7976931348623157*Math.pow(10, 308),  1.7976931348623157*Math.pow(10, 308)};

    @org.junit.jupiter.api.Test
    void pushAndPop() throws ReferenceToEmptyStack {
        for(int i = 0; i < 10; ++i)
            assertThrowsExactly(ReferenceToEmptyStack.class, stack::pop);

        for(double num : testArr){
            stack.push(num);
            double outnum = stack.pop();
            assertEquals(outnum, num);
        }
        /*
        String[] wrongFormatData = {"NotANumber", "123,444", "&!@#*&", "*98.333"};
        for(String arg: wrongFormatData){
            assertThrowsExactly(WrongFormatInputData.class, stack.push(arg));
        }*/
    }

    @org.junit.jupiter.api.Test
    void print() throws ReferenceToEmptyStack {
        for(int i = 0; i < 10; ++i)
            assertThrowsExactly(ReferenceToEmptyStack.class, stack::print);
        stack.push(0.1);
        stack.pop();
        for(int i = 0; i < 10; ++i)
            assertThrowsExactly(ReferenceToEmptyStack.class, stack::print);
    }
}