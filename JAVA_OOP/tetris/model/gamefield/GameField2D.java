package model.gamefield;

import model.Figure;
import model.exception.DimensionOutOfField;
import model.exception.IndexOutOfField;
import model.GameField;

public class GameField2D implements GameField {
    private final int X_SIZE_FLD;
    private final int Y_SIZE_FLD;
    private final int[] gameField;

    public GameField2D(){
        this(10, 20);
    }
    public GameField2D(int x, int y){
        gameField = new int[x*y];
        X_SIZE_FLD = x;
        Y_SIZE_FLD = y;

        renew();
    }

    @Override
    public int[] getRepresentation(){
        return gameField.clone();
    }

    @Override
    public boolean isCellFree(int ... pos) throws IndexOutOfField, DimensionOutOfField {
        if(pos.length != 2){
            throw new DimensionOutOfField(pos.length, 2);
        }
        if(pos[0] > X_SIZE_FLD-1){
            throw new IndexOutOfField(pos[0], X_SIZE_FLD, 'X');
        }
        if(pos[1] > Y_SIZE_FLD-1){
            throw new IndexOutOfField(pos[1], Y_SIZE_FLD, 'Y');
        }
        return gameField[pos[1] * X_SIZE_FLD + pos[0]] == 0;
    }

    @Override
    public void assignValueToPosition(Object value, int ... pos) throws IndexOutOfField, DimensionOutOfField {
        if(pos.length != 2){
            throw new DimensionOutOfField(pos.length, 2);
        }
        int x = pos[0];
        int y = pos[1];
        if(x >= X_SIZE_FLD || x < 0 ){
            throw new IndexOutOfField(x, X_SIZE_FLD, 'X');
        }
        if(y >= Y_SIZE_FLD || y < 0 ){
            throw new IndexOutOfField(y, Y_SIZE_FLD, 'Y');
        }

        Integer val = (Integer) value;
        gameField[y * X_SIZE_FLD + x] = val;
    }

    @Override
    public void renew(){
        for(int i = 0; i < Y_SIZE_FLD; ++i){
            for(int j = 0; j < X_SIZE_FLD; ++j){
                gameField[i * X_SIZE_FLD + j] = 0;
            }
        }
    }


    private final int[] sizeBuffer = new int[2];
    @Override
    public int[] getSizeField(){
        sizeBuffer[0] = X_SIZE_FLD;
        sizeBuffer[1] = Y_SIZE_FLD;
        return sizeBuffer;
    }

    @Override
    public int removeFullLayers(int from, int length){
        int deletedLayersCounter = 0;
        int posCheck = from + length - 1;
        for(int i = 0; i < 4; ++i){
            int j = 0;
            for(; j < X_SIZE_FLD; ++j){
                if( gameField[posCheck * X_SIZE_FLD + j] == 0){
                    break;
                }
            }
            if(j == X_SIZE_FLD){
                for(int m = posCheck ; m > 0; --m) {
                    for (int k = 0; k < X_SIZE_FLD; ++k) {
                        gameField[m * X_SIZE_FLD + k] = gameField[(m-1) * X_SIZE_FLD + k];
                    }
                }
                ++deletedLayersCounter;
            } else {
                --posCheck;
            }
        }
        return deletedLayersCounter;
    }

}
