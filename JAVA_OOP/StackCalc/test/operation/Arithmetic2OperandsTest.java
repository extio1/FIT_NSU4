package operation;

import calcException.ConfigurationError;
import calcException.OperationException;
import calcException.ReferenceToEmptyStack;
import factory.OperationFactory;
import org.junit.jupiter.api.Test;
import stack.MyStack;
import factory.OperationFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*
public class Arithmetic2OperandsTest<OperationT extends Operation<Double>> {
    private GeneralResourses resourses;
    private final Operation<Double> operation = Class.forName(OperationT);

    public Arithmetic2OperandsTest() throws ConfigurationError {
    }

    @Test
    void apply() throws OperationException, ReferenceToEmptyStack {
        MyStack<Double> stack = resourses.stack();
        for(double val : resourses.testData()) {
            stack.push(val);
            operation.apply(stack);
            assertEquals(stack.pop(), Math.sqrt(val));
        }
    }
}*/
