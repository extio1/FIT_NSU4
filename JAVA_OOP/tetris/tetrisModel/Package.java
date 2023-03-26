package tetrisModel;

public class Package {
    private final int X_SIZE_FLD;
    private final int Y_SIZE_FLD;

    private int[][] gameField;
    private byte[] figureView;
    private int figureLength;
    private int figureWidth;

    Package(int xsf, int ysf){
        X_SIZE_FLD = xsf;
        Y_SIZE_FLD = ysf;
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
    }
}
