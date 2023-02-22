package calcException;

public class ConfigFileHaventOpened extends FactoryException{
    public ConfigFileHaventOpened(String _filename){
        filename = _filename;
    }
    public String getConfigName(){
        return filename;
    }

    @Override
    public String getMessage(){
        return "File <"+filename+"> have not opened.\n";
    }

    private final String filename;
}
