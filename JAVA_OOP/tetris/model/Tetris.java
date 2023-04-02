package model;

import model.exception.*;
import model.gamefield.GameField2D;
import viewContrComm.Package;

import observation.Subject;
import observation.Observer;

import java.util.*;

public class Tetris implements Subject {
    private static final byte N_FIGURES_IN_TETRIS = 7;

    private final List<Observer> observers = new ArrayList<>();

    private volatile boolean gameRunning = false;

    private final Random randomGenerator;
    private int figureCounter;
    private int figureFallingPeriod = 1000; //sleep in ms

    private final Figure[] tetrisFigures;
    private Figure[] fallingFigure;
    private final GameField gameField;

    private Thread figureFalls;

    public Tetris(){
        this(10, 20);
    }

    public Tetris(int xDim, int yDim){
        gameField = new GameField2D(xDim, yDim);
        figureCounter = 1;
        tetrisFigures = Figure.values();
        randomGenerator = new Random();
        fallingFigure = new Figure[2];
        firstFigureUnit();
        generateFigureFallingThread();
    }

    private void generateFigureFallingThread(){
        figureFalls  = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Thread.interrupted() || gameRunning) {
                    down();
                    signalyzeAll();
                    try {
                        Thread.sleep(figureFallingPeriod);
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
        generateNewFallingFigure();
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

    public void down(){
        if(gameRunning) {
            try{
                fallingFigure[0].moveDown(gameField);
            } catch (ImpossibleToMoveFigureDown e){
                figureFell();
                generateNewFallingFigure();
            }
            signalyzeAll();
        }
    }

    public void left(){
        if(gameRunning) {
            try {
                fallingFigure[0].moveLeft(gameField);
            } catch (ImpossibleToMoveFigureLeft e){
                return;
            }
            signalyzeAll();
        }
    }

    public void right(){
        if(gameRunning) {
            try {
                fallingFigure[0].moveRight(gameField);
            } catch (ImpossibleToMoveFigureRight e){
                return;
            }
            signalyzeAll();
        }
    }

    public void up(){
        if(gameRunning) {
            try {
                fallingFigure[0].rotate(gameField);
            } catch (ImpossibleToRotateFigure e){
                return;
            }
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
        Package packageToObserver = new Package(gameField.getSizeField());
        packageToObserver.setFigure(fallingFigure[0]);
        packageToObserver.setNextFigure(fallingFigure[1]);
        packageToObserver.setGameField((int[]) gameField.getRepresentation());
        return packageToObserver;
    }


    private void generateNewFallingFigure(){
        figureCounter += 10;
        fallingFigure[0] = fallingFigure[1];
        fallingFigure[1] = tetrisFigures[(randomGenerator.nextInt(N_FIGURES_IN_TETRIS))];
        fallingFigure[1].newFigure(figureCounter);
    }

    private void firstFigureUnit(){
        fallingFigure[1] = tetrisFigures[(randomGenerator.nextInt(N_FIGURES_IN_TETRIS))];
    }

    private void figureFell(){
        try {
            fallingFigure[0].addFigureToField(gameField);
        } catch(FigureAddingToFieldException e){
            System.out.println(e.getMessage());
        }
    }

}
