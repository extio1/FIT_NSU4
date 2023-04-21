package model;

import model.exception.DimensionOutOfField;
import model.exception.IndexOutOfField;

public interface GameField {
    Object getRepresentation();
    boolean isCellFree(int ... pos) throws IndexOutOfField, DimensionOutOfField;
    void assignValueToPosition(Object value, int ... pos) throws IndexOutOfField, DimensionOutOfField;
    int[] getSizeField();
    void renew();
    /**
     *
     * @param from current position of the figure
     * @param length it's lenght
     * @return num of layers was deleted
     */
    int removeFullLayers(int from, int length);
}
