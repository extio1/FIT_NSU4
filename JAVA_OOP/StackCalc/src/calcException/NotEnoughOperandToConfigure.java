package calcException;

import operation.CustomizableOperation;

public class NotEnoughOperandToConfigure extends OperationConfigurationError{
    public NotEnoughOperandToConfigure(CustomizableOperation operation){
        super(operation);
    }

    @Override
    public String getMessage(){
        return "Not enough operands to configure <- " + super.getMessage();
    }
}
