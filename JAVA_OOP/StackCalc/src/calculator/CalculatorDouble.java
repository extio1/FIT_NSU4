package calculator;

import calcException.*;
import main.Main;
import operation.ArithmeticOperation;
import operation.ContextOperation;
import operation.Operation;
import operation.StackOperation;
import runtimeContext.RuntimeContextD;
import stack.MyStack;

import java.util.logging.Level;

public class CalculatorDouble implements Calculator<Double> {
    private final MyStack<Double> stack = new MyStack<>();
    private final RuntimeContextD context = new RuntimeContextD();

    @Override
    public void execute(Operation<Double> operation) {
        Main.logger.log(Level.INFO, operation.toString()+" starts to execute by calculator.");
        if(operation instanceof ArithmeticOperation){
            try {
                operation.apply(stack);
            } catch (OperationException e){
                System.out.println(e.getMessage());
                Main.logger.log(Level.WARNING, operation.toString()+"---"+e.getMessage());
            }
        }
        else if (operation instanceof StackOperation){
            try {
                operation.apply(stack, context);
            } catch (StackException | OperationException | OperationConfigurationError e) {
                System.out.println(e.getMessage());
                Main.logger.log(Level.WARNING, operation.toString()+"---"+e.getMessage());
            }
        }
        else if (operation instanceof ContextOperation){
            try {
                operation.apply(context);
            } catch (OperationException e) {
                System.out.println(e.getMessage());
                Main.logger.log(Level.WARNING, operation.toString()+"---"+e.getMessage());
            }
        }
    }
}
