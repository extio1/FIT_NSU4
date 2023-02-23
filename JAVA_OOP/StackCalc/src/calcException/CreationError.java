package calcException;

public class CreationError extends FactoryException{
    CreationError(String _wrongOperationName){
        wrongOperationName = _wrongOperationName;
    }

    @Override
    public String getMessage(){
        return "Operation name <"+wrongOperationName+"> ";
    }

    final String wrongOperationName;
}
