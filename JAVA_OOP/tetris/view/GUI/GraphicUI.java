package view.gui;

import controller.Controller;
import tetrisModel.Package;
import view.observation.*;

import javax.swing.*;

public class GraphicUI implements Observer {
    private final Controller controller;
    private final Subject model;

    private JFrame mainFrame;
    private MainWindow mainWindow;

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

    public GraphicUI(int width, int height, Controller _controller, Subject _subject) throws InterruptedException {
        controller = _controller;
        model = _subject;

        mainFrame = new JFrame("Tetris");
        mainFrame.setSize(600, 800);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setIconImage(new ImageIcon("resources/images/main_frame_icon.jpg").getImage());

        mainWindow = new MainWindow(width, height, controller, this);

        updater.start();
    }

    public void killMe(){
        updater.interrupt();
    }

    @Override
    public void update() {
        flagDataChanges = true;
    }

    private void changeImage(){
        mainWindow.updateWindow((Package) model.getInfo());
    }

}
