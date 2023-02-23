package calcException;

public class ConfigFileHaventOpened extends ConfigurationError{
    public ConfigFileHaventOpened(String _filename){
        super(_filename);
    }

    @Override
    public String getMessage(){
        return "File have not opened <-"+super.getMessage()+"> ";
    }

}
