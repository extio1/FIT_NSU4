
import operation.Operation;

public interface Calculator<T> {
    void execute(Operation<T> operation);
}
