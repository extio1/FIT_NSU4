package calcException;

import operation.Operation;

public class ContextException extends OperationException{
    public ContextException(Operation<?> operation){
        super(operation);
    }

    @Override
    public String getMessage(){
        return "Context operation exception <- "+super.getMessage();
    }
}
