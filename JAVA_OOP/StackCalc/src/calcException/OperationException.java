package calcException;

import operation.Operation;

public class OperationException extends Throwable{
    OperationException(Operation<?> operation){
        operationName = operation.toString();
    }
    @Override
    public String getMessage(){
        return "Operation <"+operationName+"> error. ";
    }

    final String operationName;
}
