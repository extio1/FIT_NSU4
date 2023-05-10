package client.exception;

public class ConfigurationException extends Exception{
    @Override
    public String getMessage(){
        return "Configuration error";
    }
}
