package tetrisModel;

public class Package {
    private boolean fieldChanges;

    private final int X_SIZE_FLD;
    private final int Y_SIZE_FLD;

    private int[][] gameField;
    private byte[] figureView;
    private int figureLength;
    private int figureWidth;
    private int figureNumber;
    private int figurePosX;
    private int figurePosY;

    Package(int xsf, int ysf){
        X_SIZE_FLD = xsf;
        Y_SIZE_FLD = ysf;
        fieldChanges = true;
        gameField = new int[X_SIZE_FLD][Y_SIZE_FLD];
    }

    public void setFigurePosX(int posX){
        figurePosX = posX;
    }

    public void setFigurePosY(int posY){
        figurePosY = posY;
    }

    public void setFigureNumber(int fn){
        figureNumber = fn;
    }

    public void setGameField(int[][] gf){
        for(int i = 0; i < X_SIZE_FLD; ++i){
            for(int j = 0; j < Y_SIZE_FLD; ++j){
                gameField[i][j] = gf[i][j];
            }
        }
    }

    public void setFigure(Figure figure) {
        figureView = figure.params.getCondition().clone();
        figureLength = figure.params.getLength();
        figureWidth = figure.params.getWidth();
        figurePosX = figure.params.getPosX();
        figurePosY = figure.params.getPosY();
        figureNumber = figure.params.getOrdinal();
    }

    public int getFigureNumber() {
        return figureNumber;
    }

    public int getFigurePosX() {
        return figurePosX;
    }

    public int getFigurePosY() {
        return figurePosY;
    }

    public void setFieldChanges(boolean flag){
        fieldChanges = flag;
    }

    public boolean isFieldChanges(){
        return fieldChanges;
    }

    public int[][] getGameField(){
        return gameField;
    }

    public byte[] getFigureView(){
        return figureView;
    }

    public int getFigureLength(){
        return figureLength;
    }

    public int getFigureWidth() {
        return figureWidth;
    }

}
