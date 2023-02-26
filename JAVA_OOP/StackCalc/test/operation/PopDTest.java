package operation;

import calcException.NotDefined;
import calcException.NotEnoughOperandToConfigure;
import calcException.ReferenceToEmptyStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PopDTest {
    private final GeneralResourses resourses = new GeneralResourses();
    private final PopD pop = new PopD();

    @Test
    void apply() throws ReferenceToEmptyStack, NotEnoughOperandToConfigure, NotDefined {
        assertThrowsExactly(ReferenceToEmptyStack.class, ()->pop.apply(resourses.stack, resourses.context));

        resourses.stack.push(5.0);
        pop.apply(resourses.stack, resourses.context);
        assertThrowsExactly(ReferenceToEmptyStack.class, ()->pop.apply(resourses.stack, resourses.context));
    }
}