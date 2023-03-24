package tetrisModel;

import controller.CommandTetris;
import view.observation.Observer;
import view.observation.Subject;

import java.util.*;

public class Tetris implements Subject {

    private volatile boolean gameRunning = false;

    private int figureCounter = 0;
    private int figureSpeed = 1000; //sleep in ms
    private final List<Observer> observers = new ArrayList<>();
    
    private int[] gameField;
    // there should be two data: outer and for observers

    Thread figureFalls;

    public Tetris(){
        final int FIELD_SIZE_X = 10;
        final int FIELD_SIZE_Y = 20;

        gameField = new int[FIELD_SIZE_X * FIELD_SIZE_Y];

        for(int i = 0; i < FIELD_SIZE_X; ++i){
            for(int j = 0; j < FIELD_SIZE_Y; ++j){
                gameField[i * FIELD_SIZE_X + j] = 0;
            }
        }
    }

    private void generateFigureFallingThread(){
        figureFalls  = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Thread.interrupted() || gameRunning) {
                    System.out.println("GO DOWN");
                    signalyzeAll();
                    try {
                        Thread.sleep(figureSpeed);
                    } catch (InterruptedException e) {
                        if(gameRunning) {
                            System.out.println(e.getMessage());
                        }
                        break;
                    }
                }
            }
        });
        figureFalls.setPriority(Thread.MAX_PRIORITY);
    }

    public void turnOn(){
        gameRunning = true;
        figureFalls.start();
    }
    public void pauseGame(){
        gameRunning = false;
    }

    public void exit() {
        if(gameRunning) {
            gameRunning = false;
            figureFalls.interrupt();
            System.out.println("Exit!");
        }
    }
    public void highScores() {
        if(gameRunning) {
            figureFalls.interrupt();
            System.out.println("about");
        }
    }
    public void about() {
        if(gameRunning) {
            figureFalls.interrupt();
            System.out.println("about");
        }
    }
    public void newGame() {
        if(gameRunning) {
            gameRunning = false;
            figureFalls.interrupt();
            System.out.println("about");
        }
    }
    public void menu() {
        if(gameRunning) {
            figureFalls.interrupt();
            System.out.println("menu");
        }
    }

    public void left(){
        if(gameRunning) {
            System.out.println("DO LEFT");
            signalyzeAll();
        }
    }

    public void right(){
        if(gameRunning) {
            System.out.println("DO RIGHT");
            signalyzeAll();
        }
    }

    public void up(){
        if(gameRunning) {
            System.out.println("DO UP");
            signalyzeAll();
        }
    }

    public void down(){
        if(gameRunning) {
            System.out.println("DO DOWN");

            signalyzeAll();
        }
    }

    @Override
    public void attach(Observer obs) {
        observers.add(obs);
    }

    @Override
    public void detach(Observer obs) {
        observers.remove(obs);
    }

    @Override
    public void signalyzeAll() {
        for(Observer obs : observers){
            obs.update();
        }
    }

    @Override
    public Object getInfo() {
        return gameField;
    }
}
