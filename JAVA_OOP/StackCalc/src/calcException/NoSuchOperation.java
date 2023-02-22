package calcException;

public class NoSuchOperation extends FactoryException{
    public NoSuchOperation(String wrongOperationName){

    }

    private String wrongOperationName;
}
