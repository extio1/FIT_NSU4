package calcException;

import operation.Operation;

public class OperationNotEnoughOperands extends OperationException{
    public OperationNotEnoughOperands(Operation<?> operation) {
        super(operation);
    }
    @Override
    public String getMessage(){
        return "Not enough operands for operation <- "+ super.getMessage();
    }
}
