package operation;

import calcException.OperationNotEnoughOperands;
import calcException.ReferenceToEmptyStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlusDTest {
    private final GeneralResourses resourses = new GeneralResourses();
    private final PlusD operation = new PlusD();

    @Test
    void apply() throws ReferenceToEmptyStack, OperationNotEnoughOperands {
        assertThrowsExactly(OperationNotEnoughOperands.class, ()->operation.apply(resourses.stack));
        for(double val1 : GeneralResourses.testData) {
            for(double val2 : GeneralResourses.testData) {
                resourses.stack.push(val1);
                resourses.stack.push(val2);
                operation.apply(resourses.stack);
                assertEquals(resourses.stack.pop(), val1 + val2);
            }
        }

    }
}