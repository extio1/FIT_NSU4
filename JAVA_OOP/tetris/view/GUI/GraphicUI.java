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

    private volatile boolean flagDataChanges = false;

    Thread updater = new Thread(new Runnable() {
        @Override
        public void run() {
            while(!Thread.interrupted()){
                if(flagDataChanges){
                    System.out.println("Data updated, asking for model");
                    //changeImage();
                    flagDataChanges = false;
                }
            }
        }
    });

    public void killMe(){
        updater.interrupt();
    }

    public GraphicUI(int width, int height, Controller _controller, Subject _subject){
        controller = _controller;
        model = _subject;

        mainFrame = new JFrame("Tetris");
        mainFrame.setSize(600, 800);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setIconImage(new ImageIcon("resources/images/main_frame_icon.jpg").getImage());

        mainWindow = new MainWindow(controller, this);

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
