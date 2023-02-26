package operation;

import calcException.NotDefined;
import calcException.NotEnoughOperandToConfigure;
import calcException.OperationException;
import calcException.ReferenceToEmptyStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

class PushDTest {
    private final GeneralResourses resourses = new GeneralResourses();
    private final PushD oper = new PushD();

    @Test
    void apply() throws OperationException, NotEnoughOperandToConfigure, ReferenceToEmptyStack {
        String[] arg = {"PUSH", "A"};
        oper.set(arg);
        assertThrowsExactly(NotDefined.class, ()->oper.apply(resourses.stack, resourses.context));

        arg = new String[]{"PUSH", "3,14"};
        oper.set(arg);
        assertThrowsExactly(NotDefined.class, ()->oper.apply(resourses.stack, resourses.context));

        // 2+2 = 4
        resourses.context.define("SOME_VALUE", 2.0);
        arg = new String[]{"SOME_VALUE", "2.0"};
        oper.set(arg);
        oper.apply(resourses.stack, resourses.context);
        oper.apply(resourses.stack, resourses.context);
        Operation<Double> plus = new PlusD();
        plus.apply(resourses.stack);
        assertEquals(resourses.stack.pop(), 4.0);

        for(Double var : GeneralResourses.testData){
            arg = new String[]{"PUSH", var.toString()};
            oper.set(arg);
            oper.apply(resourses.stack, resourses.context);
            Double varInStack = resourses.stack.pop();
            assertEquals(var, varInStack);
        }
    }
}