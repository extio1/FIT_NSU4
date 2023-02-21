package calcException;

public class FactoryException extends Exception{
    FactoryException(){
        super("The try of making unknown operation");
    }
    FactoryException(operation.Operation op){
        super("The operation "+op.toString()+"doesn't exist.");
    }
}
