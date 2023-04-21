package model;

import main.Main;
import model.exception.*;
import model.gamefield.GameField2D;

import observation.Subject;
import observation.Observer;

import java.io.*;
import java.util.*;

public class Tetris extends Thread implements Subject {
    private static final byte N_FIGURES_IN_TETRIS = 7;
    private static final byte COLOR_STEP = 10;
    private static final int TIME_FALLING_PERIOD_START = 1000;
    private static final String STAT_FILE_PATH = "model/stat.txt";

    private final Map<Observer, Package> observers = new HashMap<>();

    private volatile boolean gameRunning;

    private final Random randomGenerator;
    private int figureFallingPeriod = TIME_FALLING_PERIOD_START; //sleep in ms
    private int gameScore = 0;

    private final Figure[] tetrisFigures;
    private Figure fallingFigure;
    private int nextFigureNum;
    private final GameField gameField;
    private int currColor;
    private int nextColor;

    private Map<String, Integer> playersStat = new TreeMap<>();
    private volatile Package.PackageState stateForOutcomePackage;

    public Tetris(){
        this(10, 20);
    }

    public Tetris(int xDim, int yDim) {
        gameField = new GameField2D(xDim, yDim);
        tetrisFigures = Figure.values();
        randomGenerator = new Random();

        readGameStatFile();
        gameRunning = false;
        this.start();
    }

    @Override
    synchronized public void run() {
        while(!interrupted()) {
            if(!gameRunning){
                try {
                    this.wait();
                    System.out.println("Waken up");
                } catch (InterruptedException e) {
                    break;
                }
            }

            down();
            stateForOutcomePackage = Package.PackageState.defaultState;
            signalizeAll();
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

    private void readGameStatFile(){
        try(ObjectInputStream bis = new ObjectInputStream(new FileInputStream(STAT_FILE_PATH))) {
            playersStat = (TreeMap<String, Integer>) bis.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    private void writeGameStatFile(){
        try(ObjectOutputStream bis = new ObjectOutputStream(new FileOutputStream(STAT_FILE_PATH))) {
            System.out.println(playersStat.hashCode());
            bis.writeObject(playersStat);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void launch(){
        newGame();
        stateForOutcomePackage = Package.PackageState.authState;
        readGameStatFile();
        signalizeAll();
    }

    public void exit() {
        System.out.println("EXIT");
        gameRunning = false;
        writeGameStatFile();
        this.interrupt();
    }

    public void highScores() {
        System.out.println("NIGH SCORES");
        gameRunning = false;
        stateForOutcomePackage = Package.PackageState.onlyScoresState;
        signalizeAll();
    }

    public void about() {
        System.out.println("ABOUT");
        gameRunning = false;
        stateForOutcomePackage = Package.PackageState.onlyAboutState;
        signalizeAll();
    }

    /**
     * В начале спрашиваем имя игрока - отправляем во view пакет в режиме authState
     * пользователь вписывет своё имя.
     */

    public void newGame() {
        System.out.println("NEW GAME");
        renewGameState();
    }

    private void renewGameState(){
        gameField.renew();
        currColor = 1;
        nextColor = currColor + COLOR_STEP;

        gameScore = 0;
        nextFigureNum = 6;
        figureFallingPeriod = TIME_FALLING_PERIOD_START;
        generateNewFallingFigure();
    }

    public void pauseGame() {
        gameRunning = false;
    }

    public void continueGame() {
        System.out.println("CONTINUE");
        synchronized (this) {
            if (!gameRunning) {
                gameRunning = true;
                System.out.println(Thread.currentThread()+"teris notifyyed");
                this.notifyAll();
            }
        }
    }

    public void down(){
        System.out.println("DOWN");
        if(gameRunning) {
            try{
                fallingFigure.moveDown(gameField);
            } catch (ImpossibleToMoveFigureDown e){
                figureFell();
                generateNewFallingFigure();
            }
            stateForOutcomePackage = Package.PackageState.defaultState;
            signalizeAll();
        }
    }

    public void left(){
        System.out.println("LEFT");
        if(gameRunning) {
            try {
                fallingFigure.moveLeft(gameField);
            } catch (ImpossibleToMoveFigureLeft e){
                return;
            }
            stateForOutcomePackage = Package.PackageState.defaultState;
            signalizeAll();
        }
    }

    public void right(){
        System.out.println("RIGHT");
        if(gameRunning) {
            try {
                fallingFigure.moveRight(gameField);
            } catch (ImpossibleToMoveFigureRight e){
                return;
            }
            stateForOutcomePackage = Package.PackageState.defaultState;
            signalizeAll();
        }
    }

    public void up(){
        System.out.println("UP");
        if(gameRunning) {
            try {
                fallingFigure.rotate(gameField);
            } catch (ImpossibleToRotateFigure e){
                return;
            }
            stateForOutcomePackage = Package.PackageState.defaultState;
            signalizeAll();
        }
    }

    @Override
    public void attach(Observer obs) {
        observers.put(obs, new Package(gameField.getSizeField()));
    }

    @Override
    public void detach(Observer obs) {
        observers.remove(obs);
    }

    @Override
    public void signalizeAll() {
        observers.forEach((observer, pcg) -> observer.update());
    }

    @Override
    public Object getInfo(Observer obs) throws UnattachedObserverException {
        Package packageToObserver = observers.get(obs);
        if(packageToObserver == null){
            throw new UnattachedObserverException(obs);
        }

        packageToObserver.setPlayersStatistics(playersStat);
        packageToObserver.setState(stateForOutcomePackage);
        packageToObserver.setScore(gameScore);
        packageToObserver.setSpeed((float) TIME_FALLING_PERIOD_START / (float) figureFallingPeriod);
        packageToObserver.setFigure(fallingFigure);
        packageToObserver.setNextFigure(tetrisFigures[nextFigureNum], nextColor);
        packageToObserver.setGameField((int[]) gameField.getRepresentation());
        packageToObserver.setCurrColor(currColor);
        packageToObserver.setNextColor(nextColor);

        return packageToObserver;
    }


    private void generateNewFallingFigure(){
        currColor = nextColor;
        nextColor += COLOR_STEP;
        fallingFigure = tetrisFigures[nextFigureNum];
        fallingFigure.refresh();

        nextFigureNum = 6;
        tetrisFigures[nextFigureNum].refresh();
    }

    private void figureFell(){
        try {
            if(fallingFigure.doesntFitOnField()) {
                gameRunning = false;
            } else {
                fallingFigure.addFigureToField(gameField, currColor);
                int layersDeleted = gameField.removeFullLayers(fallingFigure.getDescriptor().getPosY(),
                                                               fallingFigure.getDescriptor().getLength());
                figureFallingPeriod -= layersDeleted * 100;
                gameScore += currColor + layersDeleted * 100;
            }
        } catch(FigureAddingToFieldException e){
            System.out.println(e.getMessage());
        }
    }
}
