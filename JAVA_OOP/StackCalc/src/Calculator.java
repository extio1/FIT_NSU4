import calcException.NotEnoughOperands;
import operation.Operation;

public interface Calculator<T> {
    void execute(Operation<T> operation) throws NotEnoughOperands;
}
