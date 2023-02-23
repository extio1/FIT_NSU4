package calcException;

import operation.Operation;

public class ArithmeticException extends OperationException{
    ArithmeticException(Operation<?> op){
        super(op);
    }
    @Override
    public String getMessage(){
        return " Arithmetic Exception <-" + super.getMessage();
    }
}
