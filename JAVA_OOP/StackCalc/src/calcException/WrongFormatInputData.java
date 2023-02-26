package calcException;

import operation.CustomizableOperation;

public class WrongFormatInputData extends OperationConfigurationError{
    public WrongFormatInputData(CustomizableOperation operation, String inString){
        super(operation);
        wrongData = inString;
    }

    @Override
    public String getMessage(){
        return "Wrong format input data, string \""+wrongData+"\" cannot be converted into required type <- "+super.getMessage();
    }

    final String wrongData;
}
