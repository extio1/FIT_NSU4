package calcException;

public class ConfigurationError extends FactoryException{
    public ConfigurationError(){}
    public ConfigurationError(String _configName){
        configName = _configName;
    }

    void setConfigName(String fileName){
        configName = fileName;
    }

    @Override
    public String getMessage(){
        return "Fabric configuration error with config file <"+configName+"> <- "+super.getMessage();
    }

    String configName;
}
