package calcException;

public class NoSuchOperation extends CreationError{
    public NoSuchOperation(String _wrongOperationName){
        super(_wrongOperationName);
    }
    @Override
    public String getMessage(){
        return "Operation doesn't exist <- "+super.getMessage();
    }
}
