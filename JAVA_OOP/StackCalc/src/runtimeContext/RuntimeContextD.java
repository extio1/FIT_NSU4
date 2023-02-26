package runtimeContext;
import java.util.Objects;
import java.util.TreeMap;

public class RuntimeContextD implements RuntimeContext<Double> {
    private final TreeMap<String, Double> data = new TreeMap<>();

    @Override
    public void define(String key, Double value) {
        data.merge(key, value, (x, y) -> y);
    }

    @Override
    public Double getIfDefined(String key){
        return data.get(key);
    }
}
