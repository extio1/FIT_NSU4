package view.gui;

import javax.swing.*;
import java.awt.*;

public class GameField extends JComponent {
    private int xSize;
    private int ySize;

    GameField(int xSize, int ySize){
        this.xSize = xSize;
        this.ySize = ySize;
        JTable field = new JTable();
        add(field);
    }
    @Override
    public void paintComponent(Graphics g){
        Graphics2D mainRect = (Graphics2D) g;
        mainRect.setColor(new Color(63, 76, 62, 255));
        mainRect.fillRect(450, 0, 180, 800);
    }
}
