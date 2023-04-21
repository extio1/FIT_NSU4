package model.figure;

import model.Figure;

public class FigureDescriptor {
    private byte[][] allPossibleView;
    private byte[] currentView;
    private byte initCondLen;
    private byte initCondWidth;
    private byte length;
    private byte width;
    private int posX;
    private int posY;

    private final Object[] buff = new Object[6];

    public FigureDescriptor(Figure figure){
        setFigureDescriptorBy(figure);
    }

    public void setFigureDescriptorBy(Figure figure){
        figure.getRepresentationParams(buff);
        initCondLen = (byte) buff[Figure.FigureParam.length.ordinal()];
        initCondWidth = (byte) buff[Figure.FigureParam.width.ordinal()];
        allPossibleView = (byte[][]) buff[Figure.FigureParam.allConditions.ordinal()];
    }

    public void renew(Figure figure){
        figure.getVolatileParams(buff);
        currentView = (byte[]) buff[Figure.FigureParam.condition.ordinal()];
        length = (byte) buff[Figure.FigureParam.length.ordinal()];
        width = (byte) buff[Figure.FigureParam.width.ordinal()];
        posX = (int) buff[Figure.FigureParam.posX.ordinal()];
        posY = (int) buff[Figure.FigureParam.posY.ordinal()];
        allPossibleView = (byte[][]) buff[Figure.FigureParam.allConditions.ordinal()];
    }

    public byte[][] getAllPossibleView() {
        return allPossibleView;
    }

    public int getPosY() {
        return posY;
    }

    public byte getInitCondLen() {
        return initCondLen;
    }

    public byte getInitCondWidth() {
        return initCondWidth;
    }

    public int getPosX() {
        return posX;
    }

    public byte[] getCurrentView() {
        return currentView;
    }

    public byte getWidth() {
        return width;
    }

    public byte getLength() {
        return length;
    }
}
