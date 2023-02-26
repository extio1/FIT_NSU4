package runtimeContext;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RuntimeContextDTest {
    private final RuntimeContextD context = new RuntimeContextD();
    double[] testVal = {0, 5, 250.333, -250.333, 123.123123123123, -2147483648, 2147483647, 4.9*Math.pow(10, -324),
            4.9*Math.pow(10, -324),  1.7976931348623157*Math.pow(10, 308),  1.7976931348623157*Math.pow(10, 308)};

    @Test
    void define() {
        StringBuilder builder = new StringBuilder("A");
        for(double val : testVal) {
            String key = builder.toString();
            context.define(key, val);
            assertEquals(val, context.getIfDefined(key));
            builder.append('A');
        }

        context.define("KEY", 1.1);
        context.define("KEY", 1.2);
        assertEquals(1.2, context.getIfDefined("KEY"));

    }

    @Test
    void getIfDefined() {
        assertNull(context.getIfDefined("KEY"));
        context.define("KEY", 1.1);
        assertEquals(1.1, context.getIfDefined("KEY"));
        assertNull(context.getIfDefined("ANY_OTHER_KEY"));
    }
}