package viewer.gui;

import model.figure.FigureDescriptor;

import javax.swing.*;
import java.awt.*;

class GameField extends JComponent {
    private static final int X_FIELD_SIZE = 10;
    private static final int Y_FIELD_SIZE = 20;
    private static final int CELL_SIZE = 35;
    private static final int BEGIN_FIELD_X = 15;
    private static final int BEGIN_FIELD_Y = 15;

    private int[] gameField;
    private byte[] figureView;
    private int figureWidth;
    private int figureLength;
    private int figurePosX;
    private int figurePosY;
    private int figureColor;

    GameField(){
        gameField = new int[X_FIELD_SIZE * Y_FIELD_SIZE];
        setBounds(0,0, 500,800);
    }

    public void updateField(int[] gf){
        gameField = gf;
        repaint();
    }

    public void updateFallingFigure(FigureDescriptor fd, int color){
        figureView =    fd.getCurrentView();
        figureWidth =   fd.getWidth();
        figureLength =  fd.getLength();
        figurePosX =    fd.getPosX();
        figurePosY =    fd.getPosY();
        figureColor =   color;
        repaint();
    }

    @Override
    public void paintComponent(Graphics g){
        g.setColor(new Color(229, 255, 234, 255));
        g.fillRect(0, 0, 400, 800);

        drawFallingFigure(g);
        drawField(g);


        g.setColor(new Color(158, 182, 185,255));
        g.drawLine(15,15,365, 15);
        g.drawLine(15,15,15, 715);
        g.drawLine(365,15,365, 715);
        g.drawLine(15,715,365, 715);

        g.drawLine(10,20,15, 20);
        g.drawLine(10,20,10, 710);
        g.drawLine(20,10,20,15);
        g.drawLine(360,10,360,15);
        g.drawLine(365,20,370,20);
        g.drawLine(10,710,15, 710);
        g.drawLine(20,10,360, 10);
        g.drawLine(20,715,20, 720);
        g.drawLine(370,20,370, 710);
        g.drawLine(20,720,360, 720);
        g.drawLine(360,715,360, 720);
        g.drawLine(365,710,370, 710);

        for(int i = 0; i < X_FIELD_SIZE; ++i){
            g.drawLine(15+CELL_SIZE*i,15,15+CELL_SIZE*i, 715);
        }

        for(int i = 0; i < Y_FIELD_SIZE; ++i){
            g.drawLine(15,15+CELL_SIZE*i,365, 15+CELL_SIZE*i);
        }
    }

    private void drawField(Graphics g){
        for(int i = 0; i < Y_FIELD_SIZE; ++i){
            for(int j = 0; j < X_FIELD_SIZE; ++j){
                int val = gameField[i * X_FIELD_SIZE + j];
                if(val != 0){
                    g.setColor(new Color(val*10%256, val*5%256,val*2%256));
                    g.fillRect(BEGIN_FIELD_X+j*CELL_SIZE, BEGIN_FIELD_Y+i*CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }
    }

    private void drawFallingFigure(Graphics g){
        g.setColor(new Color(figureColor*10%256, figureColor*5%256,figureColor*2%256));
        for(int i = 0; i < figureLength; ++i){
            for(int j = 0; j < figureWidth; ++j){
                if(figureView[i*figureWidth+j] == 1){
                    g.fillRect(BEGIN_FIELD_X+(figurePosX+j)*CELL_SIZE, BEGIN_FIELD_Y+(figurePosY+i)*CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }
    }

}
