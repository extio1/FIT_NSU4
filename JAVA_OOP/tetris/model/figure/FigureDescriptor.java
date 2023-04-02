package model.figure;

import model.Figure;

public class FigureDescriptor {
    private byte[] view;
    private byte length;
    private byte width;
    private int posX;
    private int posY;
    private int ordinal;

    private final Object[] buff = new Object[6];
    public void setFigureDescriptorBy(Figure figure){
        figure.getRepresentationParams(buff);
        view = (byte[]) buff[0];
        length = (byte) buff[1];
        width = (byte) buff[2];
        posX = (int) buff[3];
        posY = (int) buff[4];
        ordinal = (int) buff[5];
    }

    public int getPosY() {
        return posY;
    }

    public int getPosX() {
        return posX;
    }

    public byte[] getView() {
        return view;
    }

    public byte getWidth() {
        return width;
    }

    public byte getLength() {
        return length;
    }

    public int getOrdinal() {
        return ordinal;
    }
}
