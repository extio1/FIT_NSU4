package view.GUI;

import controller.CommandTetris;
import controller.Controller;
import view.observation.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GraphicUI implements Observer {
    private final Controller controller;
    private final Subject TetrisModel;

    private JFrame mainFrame;
    private JFrame menuFrame;

    private volatile boolean flagDataChanges = false;
    private int[] gameField;

    Thread updater = new Thread(new Runnable() {
        @Override
        public void run() {
            while(!Thread.interrupted()){
                if(flagDataChanges){
                    System.out.println("Data updated, asking for model");
                    flagDataChanges = false;
                }
            }
        }
    });

    public GraphicUI(int width, int height, Controller _controller, Subject _subject){
        controller = _controller;
        TetrisModel = _subject;
        createWindow(width, height);
        updater.start();
    }

    public void setVisibleMainFrame(boolean flag){
        mainFrame.setVisible(flag);
    }

    @Override
    public void update() {
        flagDataChanges = true;
        //updater.interrupt();
    }

    private void changeImage(){
        System.out.println("New Image");
    }

    private void createWindow(int width, int height){
        mainFrame = new JFrame("Tetris");
        mainFrame.setSize(width, height);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mainFrame.addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode()==KeyEvent.VK_LEFT) controller.execute(CommandTetris.Left);
                if (e.getKeyCode()==KeyEvent.VK_RIGHT) controller.execute(CommandTetris.Right);
                if (e.getKeyCode()==KeyEvent.VK_UP) controller.execute(CommandTetris.Up);
                if (e.getKeyCode()==KeyEvent.VK_DOWN) controller.execute(CommandTetris.Down);
                if (e.getKeyCode()==KeyEvent.VK_ESCAPE) controller.execute(CommandTetris.Menu);
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });

        mainFrame.setFocusable(true);

        //createMenuButton();
    }

    private void createMenuButton(){
        ImageIcon buttonIcon = new ImageIcon("C:/Users/User/IdeaProjects/FIT_NSU4/JAVA_OOP/tetris/recourses/menu_button.png");
        JButton menuButton = new JButton(buttonIcon);

        menuButton.addMouseListener( new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                controller.execute(CommandTetris.Menu);
                System.out.println("Menu clicked");
                menuButton.setFocusable(false);
            }
        });
        JToolBar mainFrameBox = new JToolBar();
        mainFrameBox.add(menuButton);
        System.out.println(buttonIcon.getImageLoadStatus() == MediaTracker.COMPLETE);
        mainFrame.add(mainFrameBox);
    }
}
