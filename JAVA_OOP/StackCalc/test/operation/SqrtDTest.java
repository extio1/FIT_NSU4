package operation;

import calcException.OperationException;
import calcException.ReferenceToEmptyStack;
import org.junit.jupiter.api.Test;
import stack.MyStack;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SqrtDTest {
    private final GeneralResourses resourses = new GeneralResourses();
    private final Operation<Double> operation = new SqrtD();

    @Test
    void apply() throws OperationException, ReferenceToEmptyStack {
        MyStack<Double> stack = resourses.stack;
        for(double val : GeneralResourses.testData) {
            stack.push(val);
            operation.apply(stack);
            assertEquals(stack.pop(), Math.sqrt(val));
        }
    }
}