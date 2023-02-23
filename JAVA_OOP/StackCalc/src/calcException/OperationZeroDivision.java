package calcException;

import operation.Operation;

public class OperationZeroDivision extends ArithmeticException{
    public OperationZeroDivision(Operation<?> op){
        super(op);
    }

    @Override
    public String getMessage(){
        return "Zero division <-" + super.getMessage();
    }

}
