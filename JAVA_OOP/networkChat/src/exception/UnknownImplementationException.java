package exception;

public class UnknownImplementationException extends ConfigurationException{
    private final String unknownName;
    public UnknownImplementationException(String unknownName){
        this.unknownName = unknownName;
    }

    @Override
    public String getMessage(){
        return "Unknown implementation :"+unknownName;
    }
}
