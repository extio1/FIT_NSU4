package model;

import model.exception.DimensionOutOfField;
import model.exception.IndexOutOfField;

public interface GameField {
    Object getRepresentation();
    boolean isCellFree(int x, int y) throws IndexOutOfField;
    void assignValueToPosition(Object value, int ... pos) throws IndexOutOfField, DimensionOutOfField;
    int[] getSizeField();
    void removeFullLayers();
}
