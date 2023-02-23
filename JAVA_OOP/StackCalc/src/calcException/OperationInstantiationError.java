package calcException;

public class OperationInstantiationError extends CreationError{
    public OperationInstantiationError(String _operationName, ReflectiveOperationException _connectedException){
        super(_operationName);
        connectedException = _connectedException;
    }

    @Override
    public String getMessage(){
        return "Error while instantiation <- "+super.getMessage();
    }

    @Override
    public StackTraceElement[] getStackTrace(){
        return connectedException.getStackTrace();
    }

    ReflectiveOperationException connectedException;
}
