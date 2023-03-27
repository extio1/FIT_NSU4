package view.gui;

import controller.CommandTetris;
import controller.Controller;
import tetrisModel.Package;
import view.observation.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GraphicUI implements Observer {
    private final Controller controller;
    private final Subject model;

    private JFrame mainFrame;
    private MainWindow mainWindow;
    private GameFieldWindow gameFieldWindow;

    private volatile boolean flagDataChanges = false;

    Thread updater = new Thread(new Runnable() {
        @Override
        public void run() {
            while(!Thread.interrupted()){
                if(flagDataChanges){
                    System.out.println("Data updated, asking for model");
                    changeImage();
                    flagDataChanges = false;
                }
            }
        }
    });

    public GraphicUI(int width, int height, Controller _controller, Subject _subject){
        controller = _controller;
        model = _subject;
/*
        mainFrame = new JFrame("Tetris");
        mainFrame.setSize(600, 800);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setIconImage(new ImageIcon("resources/images/main_frame_icon.jpg").getImage());
*/

        //gameFieldWindow = new GameFieldWindow(width, height, mainFrame);
        mainWindow = new MainWindow(controller);

        //mainFrame.add(gameFieldWindow);
/*
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

        mainFrame.setVisible(true);

 */
        updater.start();
    }


    @Override
    public void update() {
        flagDataChanges = true;
    }

    private void changeImage(){
        Package info = (Package) model.getInfo();

        System.out.println("New Image");
    }

}
