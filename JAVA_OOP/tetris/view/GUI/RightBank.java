package view.gui;

import controller.CommandTetris;
import controller.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RightBank extends JPanel {
    private final static int X_SIZE_NEXT_FIGURE_FIELD = 3;
    private final static int Y_SIZE_NEXT_FIGURE_FIELD = 4;
    private final static int CELL_SIZE = 50;

    private final Controller controller;
    private byte[] nextFigureView = null;
    private int nextFigureXSize;
    private int nextFigureYSize;
    private int figureNum;

    RightBank(Controller controller, JLayeredPane lp){
        this.controller = controller;

        setBounds(440,0,200,870);
        setLayout(null);
        setOpaque(true);
        createMenuButton(lp);
    }

    public void updateNextFigure(byte[] figureView, int sizeX, int sizeY, int fn){
        nextFigureView = figureView;
        nextFigureXSize = sizeX;
        nextFigureYSize = sizeY;
        figureNum = fn;
        repaint();
    }

    @Override
    public void paintComponent(Graphics _g){
        Graphics2D g = (Graphics2D) _g;

        g.setColor(new Color(52, 77, 79, 255));
        g.fillRect(400, 0, 200, 870); // right hood

        g.setColor(new Color(50, 94, 98, 255));
        g.fillRect(415, 20, 170, 350); // figure place

        g.setColor(new Color(figureNum*10%256,figureNum*5%256,figureNum*2%256));
        for(int i = 0; i < Y_SIZE_NEXT_FIGURE_FIELD; ++i){
            if(i < nextFigureYSize) {
                for (int j = 0; j < X_SIZE_NEXT_FIGURE_FIELD; ++j) {
                    if(j < nextFigureXSize && nextFigureView[i*nextFigureXSize+j] == 1){
                        g.fillRect(425+CELL_SIZE*j, 30+CELL_SIZE*i, CELL_SIZE, CELL_SIZE);
                    }
                }
            }
        }

        drawGrid(g);
    }

    private void createMenuButton(JLayeredPane lp){
        ImageIcon img = new ImageIcon("resources/images/menu_button.png");
        JButton menuButton = new JButton(img);
        menuButton.setRolloverIcon(new ImageIcon("resources/images/menu_button_pressed.png"));

        menuButton.setBorderPainted(false);
        menuButton.setFocusPainted(false);
        menuButton.setContentAreaFilled(false);
        menuButton.setBounds(468,680,64,64);

        menuButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                controller.execute(CommandTetris.Menu);
                System.out.println("Menu clicked");
                menuButton.setFocusable(false);
            }
        });

        lp.add(menuButton, JLayeredPane.PALETTE_LAYER);

        add(menuButton);
    }

    private void drawGrid(Graphics g){
        int CELL_SIZE = 50;
        int gridSizeX = 4;
        int gridSizeY = 5;
        g.setColor(new Color(158, 182, 185, 255));
        for(int i = 0; i < gridSizeY; ++i){
            g.drawLine(425,30+CELL_SIZE*i,575, 30+CELL_SIZE*i);
        }
        for(int i = 0; i < gridSizeX; ++i){
            g.drawLine(425+CELL_SIZE*i,30,425+CELL_SIZE*i,230);
        }
    }

}
