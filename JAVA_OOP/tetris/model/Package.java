package model;

import model.figure.FigureDescriptor;

import java.util.Map;

public class Package {
    public static int POSITIONS_IN_STAT = 5;
    private volatile int[] gameField;

    private FigureDescriptor fallingFigure;
    private FigureDescriptor nextFigure;
    private int currColor;
    private int nextColor;

    private int score;
    private float speed;

    private PackageState state;

    private static String nickname;
    private volatile Map<String,Integer> playersStatistics;

    public Package(int[] sizes){
        gameField = new int[sizes[0] * sizes[1]];
    }

    public static enum PackageState{
        defaultState,
        onlyAboutState,
        onlyScoresState,
        authState
    }

    public static void setNickname(String nickname) {
        Package.nickname = nickname;
    }

    public void setPlayersStatistics(Map<String,Integer> playersStatistics) {
        this.playersStatistics = playersStatistics;
    }

    public Map<String, Integer> getPlayersStatistics() {
        return playersStatistics;
    }

    public void setState(PackageState state) {
        this.state = state;
    }

    public PackageState getState(){
        return state;
    }

    public static String getNickname() {
        return nickname;
    }
    
    public void setGameField(int[] gf){
        gameField = gf;
    }

    public void setFigure(Figure figure) {
        FigureDescriptor fd = figure.getDescriptor();
        if(fd != null){
            fallingFigure = fd;
        }
    }

    public void setNextFigure(Figure figure, int color) {
        nextFigure = figure.getDescriptor();
    }

    public void setCurrColor(int currColor) {
        this.currColor = currColor;
    }

    public int getCurrColor() {
        return currColor;
    }

    public void setNextColor(int nextColor) {
        this.nextColor = nextColor;
    }

    public int getNextColor() {
        return nextColor;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getSpeed() {
        return speed;
    }

    public FigureDescriptor getFallingFigureDescriptor() {
        return fallingFigure;
    }
    public FigureDescriptor getNextFigureDescriptor() {
        return nextFigure;
    }

    public int[] getGameField(){
        return gameField;
    }
}
