package operation;

import calcException.OperationConfigurationError;

public interface CustomizableOperation {
    void set(String[] option) throws OperationConfigurationError;
}
