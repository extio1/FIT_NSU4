package model;

import java.io.Serializable;
import java.util.Map;

public class GameScores implements Serializable {
    private Map<String, Integer> data;
    public Map<String, Integer> getData() {
        return data;
    }
}
