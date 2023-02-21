import factory.OperationFactory;
import operation.ArithmeticOperation;
import operation.Operation;

import java.lang.reflect.Constructor;

public class Main {
    public static void main(String[] args) throws Exception {
        try(CommandParser parser = new CommandParser(args)) {
            System.out.println("Hello world!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        CalculatorDouble calculator = new CalculatorDouble();
        OperationFactory factory = new OperationFactory();

        Operation operation = factory.create("DEFINE A 8");
        Operation<Double> operation2 = factory.create("PUSH 3,14");
        calculator.execute(operation2);


    }
}