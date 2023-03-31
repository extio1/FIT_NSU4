package tetrisModel;

import view.observation.Observer;
import view.observation.Subject;

import java.util.*;

public class Tetris implements Subject {
    private static final byte N_FIGURES_IN_TETRIS = 7;
    private static int X_SIZE_FLD;
    private static int Y_SIZE_FLD;

    private enum Direction {
        UP,
        LEFT,
        DOWN,
        RIGHT
    }

    private final List<Observer> observers = new ArrayList<>();
    private final Package packageToObserver;

    private volatile boolean gameRunning = false;

    private final Random randomGenerator;
    private int figureCounter;
    private int figureFallingPeriod = 1000; //sleep in ms

    private final Figure[] tetrisFigures;
    private Figure fallingFigure;

    private final int[][] gameField;
    // there should be two data: inner and for observers

    private Thread figureFalls;

    public Tetris(){
        this(10, 20);
    }

    public Tetris(int xDim, int yDim){
        gameField = new int[xDim][yDim];

        X_SIZE_FLD = xDim;
        Y_SIZE_FLD = yDim;

        for(int i = 0; i < xDim; ++i){
            for(int j = 0; j < yDim; ++j){
                gameField[i][j] = 0;
            }
        }

        figureCounter = 1;
        tetrisFigures = Figure.values();
        packageToObserver = new Package(X_SIZE_FLD, Y_SIZE_FLD);
        randomGenerator = new Random();
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

    public void left(){
        if(gameRunning) {
            int newPosX = fallingFigure.params.getPosX() - 1;
            if (newPosX >= 0 && isFreeOnDirection(Direction.LEFT)) {
                fallingFigure.moveLeft();
                signalyzeAll();
            }
        }
    }

    public void right(){
        if(gameRunning) {
            int newPosX = fallingFigure.params.getPosX() + fallingFigure.params.getWidth();
            if (newPosX < X_SIZE_FLD && isFreeOnDirection(Direction.RIGHT)) {
                fallingFigure.moveRight();
                signalyzeAll();
            }
        }
    }

    public void up(){
        if(gameRunning) {
            if(isFreeOnDirection(Direction.UP)) {
                fallingFigure.rotate();
                signalyzeAll();
            }

            /*
            if (xPos < 0){
                // checking if it's possible to move the figure to some offset right
                if(gameField[xPos][yPos + fallingFigure.params.getWidth() + Math.abs(xPos)] != 0){
                    return;
                }
            } else if (xPos > X_SIZE_FLD) {
                // checking if it's possible to move the figure to some offset left
                if(gameField[xPos][yPos - (xPos - X_SIZE_FLD)] != 0){
                    return;
                }
            }*/



            System.out.println("DO UP");
            signalyzeAll();
        }
    }

    public void down(){
        if(gameRunning) {
            if(!isFigureFell()) {
                fallingFigure.moveDown();
            } else {
                figureFell();
                generateNewFallingFigure();
            }

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
        packageToObserver.setFigure(fallingFigure);
        packageToObserver.setGameField(gameField);
        return packageToObserver;
    }

    private boolean isFigureFell(){
        int xPos = fallingFigure.params.getPosX();
        int yPos = fallingFigure.params.getPosY();
        byte[] figureView = fallingFigure.params.getCondition();
        int length = fallingFigure.params.getLength();
        int width = fallingFigure.params.getWidth();

        //if there's something under the figure or next Y-coordinate more that Y_SIZE_FLD then figure fell
        if(yPos + length >= Y_SIZE_FLD){
            return true;
        }
        System.out.println(xPos+" - "+yPos);
        for(int i = 0; i < length; ++i){
            for(int j = 0; j < width; ++j){
                if(figureView[i*width+j]==1 && gameField[xPos + j][yPos+i+1] != 0){
                    return true;
                }
            }
        }

        return false;
    }

    private void generateNewFallingFigure(){
        figureCounter += 10;
        fallingFigure = tetrisFigures[(randomGenerator.nextInt(N_FIGURES_IN_TETRIS))];
        fallingFigure.newFigure();
        fallingFigure.params.setOrdinal(figureCounter);

        System.out.println(fallingFigure);
    }

    private void figureFell(){
        int xPos = fallingFigure.params.getPosX();
        int yPos = fallingFigure.params.getPosY();
        int length = fallingFigure.params.getLength();
        int width = fallingFigure.params.getWidth();
        byte[] figureView = fallingFigure.params.getCondition();

        for(int i = 0; i < length; ++i){
            for(int j = 0; j < width; ++j){
                if(figureView[i*width+j]==1) {
                    gameField[xPos + j][yPos + i] = figureCounter;
                }
            }
        }
    }

    private boolean isFreeOnDirection(Direction direction){
        int xPos = fallingFigure.params.getPosX();
        int yPos = fallingFigure.params.getPosY();
        int length = fallingFigure.params.getLength();
        int width = fallingFigure.params.getWidth();
        byte[] figureView = fallingFigure.params.getCondition();

        switch (direction){
            case LEFT -> {
                for(int i = 0; i < length; ++i){
                    for(int j = 0; j < width; ++j){
                        if(gameField[xPos+j-1][yPos+i] != 0 && figureView[i*width+j] == 1){
                            return false;
                        }
                    }
                }
            }
            case RIGHT -> {
                for(int i = 0; i < length; ++i){
                    for(int j = 0; j < width; ++j){
                        if(gameField[xPos+j+1][yPos+i] != 0 && figureView[i*width+j] == 1){
                            return false;
                        }
                    }
                }
            }
            case UP -> {
                int[] newPointerCoordinates = fallingFigure.getCoordinatesInFieldAfterRotate();
                int newPosX = newPointerCoordinates[0];
                int newPosY = newPointerCoordinates[1];
                if(newPosX >= X_SIZE_FLD){
                    int offsetPosX = xPos - (newPosX - X_SIZE_FLD);
                    if(gameField[offsetPosX][newPosY] != 0){
                        return false;
                    } else {
                        fallingFigure.params.setPosX(offsetPosX);
                    }
                }
                if(newPosY + length >= Y_SIZE_FLD || newPosY < 0){
                    return false;
                }
                for(int i = 0; i < length; ++i){
                    for(int j = 0; j < width; ++j){
                        if(gameField[xPos+j][yPos+i] != 0 && figureView[i*width+j] == 1){
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
}
