package model.figure;

public class FigureParams {
    private volatile int posXPointer;        // текущая позиция. Указывает на левый верхний угол
    private volatile int posYPointer;        // фигуры
    private volatile byte currentCondition;  // текущее состояние
    private volatile int ordinal;

    private final byte maximCondition;
    private final byte[][] pointerShiftByChangingCondition;
    private final byte[][] size;              // длина (0 - ширина, 1 - длина)
    private final byte[][] conditions;        // все возможные состояния фигуры

    public FigureParams(int posXPointer, int posYPointer, byte maximCondition,
                        byte[][] pointerShiftByChangingCondition, byte[][] size, byte[][] conditions){
        this.posXPointer = posXPointer;
        this.posYPointer = posYPointer;
        this.size = size;
        this.pointerShiftByChangingCondition = pointerShiftByChangingCondition;
        this.maximCondition = maximCondition;
        this.conditions = conditions;
    }

    public int getPosX(){ return posXPointer; }
    public int getPosY(){ return posYPointer; }
    public byte[] getCondition() { return conditions[currentCondition].clone(); }
    public byte[] getNextCondition() { return conditions[getNextPosNum()]; }
    public byte getNewWidth() { return size[getNextPosNum()][1]; }
    public byte getNewLength() { return size[getNextPosNum()][0]; }
    public byte getLength() { return size[currentCondition][1]; }
    public byte getWidth() { return size[currentCondition][0]; }
    public byte[] getPointerShiftToNextPos() { return pointerShiftByChangingCondition[getNextPosNum()]; }
    public int[] getNextConditionPos() {
        int nextCond = ((currentCondition + 1) % maximCondition);
        return new int[]{posXPointer+pointerShiftByChangingCondition[nextCond][0], posYPointer+pointerShiftByChangingCondition[nextCond][1]};
    }
    public int getOrdinal() {
        return ordinal;
    }

    public synchronized void setNextCondition() { currentCondition = (byte) ((currentCondition + 1) % maximCondition); }
    public void setPosX(int pos){ posXPointer = pos; }
    public void setPosY(int pos){ posYPointer = pos; }
    public void setOrdinal(int o){ ordinal = o; }

    public void refresh(){
        posXPointer = 5;
        posYPointer = 0;
        currentCondition = 0;
    }

    private int getNextPosNum(){
        return (currentCondition+1) % maximCondition;
    }

}