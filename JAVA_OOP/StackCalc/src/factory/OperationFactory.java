package factory;

import calcException.*;
import main.Main;
import operation.CustomizableOperation;
import operation.Operation;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.logging.Level;

public class OperationFactory<T> {
    private InputStream inStr = null;
    private TreeMap<String, String> commandToClass;
    private TreeMap<String, Operation<T>> alreadyCreated;

    public OperationFactory() throws ConfigurationError {
        initByConfigFile("config.txt");
    }
    public OperationFactory(String configPath) throws ConfigurationError {
        initByConfigFile(configPath);
    }

    public Operation<T> create(String opName) throws NoSuchOperation, OperationInstantiationError, NoOperationConstructor, OperationConfigurationError {
        String[] commandInfo = opName.split(" ");
        String newOperationName = commandToClass.get(commandInfo[0]);
        if(newOperationName == null){
            throw new NoSuchOperation(opName);
        }

        Operation<T> operation = alreadyCreated.get(newOperationName);
        if(operation == null){
            operation = generateOperation(newOperationName);
            alreadyCreated.merge(newOperationName, operation, (x, y) -> y);
        } else {
            Main.logger.log(Level.INFO, newOperationName+" already created, returned an existing object "+operation.toString());
        }
        if(operation instanceof CustomizableOperation op) {
            op.set(commandInfo);
        }
        return operation;
    }

    private void initByConfigFile(String configPath) throws ConfigurationError {
        inStr = OperationFactory.class.getResourceAsStream(configPath);
        if(inStr == null){
            throw new ConfigFileHaventOpened(configPath);
        }
        commandToClass = new TreeMap<String, String>();
        alreadyCreated = new TreeMap<String, Operation<T>>();

        try {
            scanConfig();
        } catch(ConfigFileParseError e){
            e.setFileName(configPath);
            throw new ConfigurationError(configPath);
        }
    }

    private void scanConfig() throws ConfigFileParseError {
        final char DELIMITER_WORD = '=';
        final char DELIMITER_LINE = ';';
        int rowCounter = 0;
        int colCounter = 0;

        StringBuilder builder = new StringBuilder();
        String key = "";
        String value = "";

        try(Reader reader = new InputStreamReader(inStr)) {
            while (reader.ready()) {
                char symbol = (char) reader.read();
                if (!Character.isISOControl(symbol)) {
                    if (symbol == DELIMITER_WORD) {
                        key = builder.toString();
                        builder.setLength(0);
                    } else if (symbol == DELIMITER_LINE) {
                        value = builder.toString();
                        commandToClass.put(key, value);
                        alreadyCreated.put(value, null);
                        builder.setLength(0);
                        ++colCounter;
                    } else {
                        builder.append(symbol);
                    }
                    ++colCounter;
                }
            }
        } catch (IOException e){
            throw new ConfigFileParseError(colCounter, rowCounter);
        }

    }

    private Operation<T> generateOperation(String OperationName) throws NoSuchOperation, NoOperationConstructor, OperationInstantiationError {
        Operation<T> newOperation = null;
        try {
            Class operationT = Class.forName(OperationName);
            Constructor constructor = operationT.getConstructor();
            newOperation = (Operation<T>) constructor.newInstance();
        } catch(ClassNotFoundException e) {
            throw new NoSuchOperation(OperationName);
        } catch(NoSuchMethodException e){
            throw new NoOperationConstructor(OperationName);
        } catch(InstantiationException | IllegalAccessException | InvocationTargetException e){
            throw new OperationInstantiationError(OperationName, e);
        }
        Main.logger.log(Level.INFO, newOperation.toString()+" is generated");
        return newOperation;
    }
}
