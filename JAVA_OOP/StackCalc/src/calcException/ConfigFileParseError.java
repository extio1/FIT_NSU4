package calcException;

public class ConfigFileParseError extends ConfigurationError{
    public ConfigFileParseError(int _colPos, int _rowPos, String _filename){
        super(_filename);
        colPos = _colPos;
        rowPos = _rowPos;
    }

    public ConfigFileParseError(int _colPos, int _rowPos){
        super();
        colPos = _colPos;
        rowPos = _rowPos;
    }

    public void setFileName(String fileName){
        super.setConfigName(fileName);
    }

    @Override
    public String getMessage(){
        return "ConfigFileParseError: The error in "+colPos+"col, "+rowPos+"row."+super.getMessage();
    }

    private final int colPos;
    private final int rowPos;
}
