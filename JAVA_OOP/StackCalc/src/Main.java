import calcException.ConfigFileHaventOpened;
import calcException.ConfigFileParseError;
import calcException.NoSuchOperation;
import calcException.NotEnoughOperands;
import factory.OperationFactory;
import operation.ArithmeticOperation;
import operation.Operation;

import java.lang.reflect.Constructor;

public class Main {
    public static void main(String[] args) throws Exception {
        try(CommandParser parser = new CommandParser(args)) {
            CalculatorDouble calculator = new CalculatorDouble();
            OperationFactory<Double> factory = new OperationFactory<>();

            while (parser.ready()) {
                String command = parser.nextLine();
                Operation<Double> op = factory.create(command);
                calculator.execute(op);
            }

        } catch (ConfigFileHaventOpened | NoSuchOperation | NotEnoughOperands | ConfigFileParseError e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}