package factory;

import calcException.NoSuchOperation;
import operation.CustomizableOperation;
import operation.Operation;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.TreeMap;

public class OperationFactory {
    private InputStream inStr = null;
    private TreeMap<String, String> commandToClass;
    private TreeMap<String, Operation<?>> alreadyCreated;

    public OperationFactory() throws IOException {
        initByConfigFile("config.txt");
    }
    public OperationFactory(String configPath) throws IOException {
        initByConfigFile(configPath);
    }

    public Operation<?> create(String opName) throws Exception {
        String[] commandInfo = opName.split(" ");
        String newOperationName = commandToClass.get(commandInfo[0]);
        if(newOperationName == null){
            throw new calcException.NoSuchOperation();
        }

        Operation<?> operation = alreadyCreated.get(newOperationName);
        if(operation != null){
            if(operation instanceof CustomizableOperation op) {
                op.set(commandInfo);
            }
        } else {
            operation = generateOperation(newOperationName);
            alreadyCreated.merge(newOperationName, operation, (x, y) -> y);
        }
        return operation;
    }

    private void initByConfigFile(String configPath) throws IOException {
        inStr = OperationFactory.class.getResourceAsStream(configPath);
        commandToClass = new TreeMap<String, String>();
        alreadyCreated = new TreeMap<String, Operation<?>>();
        scanConfig();
    }

    private void scanConfig() throws IOException {
        Reader reader = new InputStreamReader(inStr);
        final char DELIMITER_WORD = '=';
        final char DELIMITER_LINE = ';';

        StringBuilder builder = new StringBuilder();
        String key = "";
        String value = "";

        while(reader.ready()){

            char symbol = (char) reader.read();
            if(!Character.isISOControl(symbol)) {
                if (symbol == DELIMITER_WORD) {
                    key = builder.toString();
                    builder.setLength(0);
                } else if (symbol == DELIMITER_LINE) {
                    value = builder.toString();
                    commandToClass.put(key, value);
                    alreadyCreated.put(value, null);
                    builder.setLength(0);
                } else {
                    builder.append(symbol);
                }
            }
        }

        reader.close();
    }

    private Operation generateOperation(String OperationName) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> operationT = Class.forName(OperationName);
        Constructor<?> constructor = operationT.getConstructor();
        return (Operation) constructor.newInstance();
    }
}
