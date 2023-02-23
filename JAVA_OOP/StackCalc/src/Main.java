import calcException.*;
import factory.OperationFactory;
import operation.Operation;

public class Main {
    public static void main(String[] args) throws Exception {
        args = new String[]{"C:/Users/User/IdeaProjects/FIT_NSU4/JAVA_OOP/StackCalc/src/input.txt"};
        try (CommandParser parser = new CommandParser(args)) {
            CalculatorDouble calculator = new CalculatorDouble();
            OperationFactory<Double> factory = new OperationFactory<>();

            System.out.println("Ready to execute commands!");

            while (parser.ready()) {
                String command = parser.nextLine();
                if (parser.exit()){
                    break;
                }
                try {
                    Operation<Double> op = factory.create(command);
                    calculator.execute(op);
                } catch (NoSuchOperation | OperationInstantiationError |
                         NoOperationConstructor | OperationConfigurationError e) {
                    System.out.println(e.getMessage());
                }
            }
            System.out.println("Shutting down.");
        } catch (ConfigurationError e) {
            System.out.println(e.getMessage());
        }
    }
}