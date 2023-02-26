package operation;

import runtimeContext.RuntimeContextD;
import stack.MyStack;

class GeneralResourses {
    public MyStack<Double> stack = null;
    public RuntimeContextD context = null;
    public static final double[] testData = {0, 5, 250.333, -250.333, 123.123123123123, -2147483648, 2147483647, 4.9*Math.pow(10, -324),
            4.9*Math.pow(10, -324),  1.7976931348623157*Math.pow(10, 308),  1.7976931348623157*Math.pow(10, 308)};

    GeneralResourses(){
        stack = new MyStack<Double>();
        context = new RuntimeContextD();
    }
}
