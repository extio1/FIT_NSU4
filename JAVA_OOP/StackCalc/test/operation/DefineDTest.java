package operation;

import calcException.NotDefined;
import calcException.OperationConfigurationError;
import calcException.OperationNotEnoughOperands;
import calcException.ReferenceToEmptyStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefineDTest {
    private final GeneralResourses resourses = new GeneralResourses();
    private final DefineD op = new DefineD();
    private final ProductD product = new ProductD();
    private final PushD push = new PushD();

    @Test
    void apply() throws OperationConfigurationError, OperationNotEnoughOperands, NotDefined, ReferenceToEmptyStack {
        String[] args = {"DEFINE", "A", "5"};
        exec(args);

        push.set(args);
        push.apply(resourses.stack, resourses.context);
        push.apply(resourses.stack, resourses.context);

        product.apply(resourses.stack);

        assertEquals(25.0, resourses.stack.pop());
//--------------
        String[] args1 = {"DEFINE", "5", "10"};
        exec(args1);
        push.set(args1);                                // PUSH 5 -> PUSH 10
        push.apply(resourses.stack, resourses.context);
        push.set(args);                                 // PUSH A -> PUSH 5
        push.apply(resourses.stack, resourses.context);
        product.apply(resourses.stack);
        assertEquals(50, resourses.stack.pop());

    }

    private void exec(String[] command) throws OperationConfigurationError {
        op.set(command);
        op.apply(resourses.context);
    }
}