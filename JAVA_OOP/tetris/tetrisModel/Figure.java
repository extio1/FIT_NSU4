package tetrisModel;

public enum Figure {
    leftAngle   (new FigureParams(5, 0, (byte) 1,
                 new byte[][]{{0, 0}, {0, 0}, {0, 0}, {0, 0}},
                 new byte[][]{{2, 3}, {3, 2}, {2, 3}, {3, 2}},
                 new byte[][]{{1, 0, 1, 0, 1, 1},
                             {1, 1, 1, 1, 0, 0},
                             {1, 1, 0, 1, 0, 1},
                             {0, 0, 1, 1, 1, 1}})),

    rightAngle  (new FigureParams(5, 0, (byte) 1,
                 new byte[][]{{0, 0}, {0, 0}, {0, 0}, {0, 0}},
                 new byte[][]{{2, 3}, {3, 2}, {2, 3}, {3, 2}},
                 new byte[][]{{1, 0, 1, 0, 1, 1},
                             {1, 1, 1, 1, 0, 0},
                             {1, 1, 0, 1, 0, 1},
                             {0, 0, 1, 1, 1, 1}})),

    leftSnake   (new FigureParams(5, 0, (byte) 1,
                 new byte[][]{{0, 0}, {0, 0}},
                 new byte[][]{{2, 3}, {3, 2}},
                 new byte[][]{{1, 0, 1, 1, 0, 1},
                             {0, 1, 1, 1, 1, 0}})),

    rightSnake  (new FigureParams(5, 0, (byte) 1,
                 new byte[][]{{0, 0}, {0, 0}},
                 new byte[][]{{2, 3}, {3, 2}},
                 new byte[][]{{0, 1, 1, 1, 1, 0},
                             {1, 1, 0, 0, 1, 1}})),

    pedestal    (new FigureParams(5, 0, (byte) 1,
                 new byte[][]{{-1, 0}, {1, 0}, {-1, 0}, {1, 0}},
                 new byte[][]{{3, 2}, {2, 3}, {2, 3}, {2, 3}},
                 new byte[][]{{0, 1, 0, 1, 1, 1},
                              {1, 0, 1, 1, 1, 0},
                              {1, 1, 1, 0, 1, 0},
                              {0, 1, 1, 1, 0, 1} })),

    stick       (new FigureParams(5, 0, (byte) 1,
                 new byte[][]{{0, 2}, {0, -1}, {1, 1}, {-1, -2}},
                 new byte[][]{{1, 4}, {4, 1}, {1, 4}, {4, 1}},
                 new byte[][]{{1, 1, 1, 1},
                              {1, 1, 1, 1},
                              {1, 1, 1, 1},
                              {1, 1, 1, 1} })),

    cube        (new FigureParams(5, 0, (byte) 1,
                 new byte[][]{{0, 0}},
                 new byte[][]{{2, 2}},
                 new byte[][]{{1, 1, 1, 1}}));

    public final FigureParams params;
    Figure(FigureParams params){
        this.params = params;
    }

    static private int[] calcBuffer = new int[2];
    public int[] getCoordinatesInFieldAfterRotate(){
        int[] buff = params.getNextConditionPos();
        calcBuffer[0] = buff[0] + params.getPosX();
        calcBuffer[1] = buff[1] + params.getPosY();
        return calcBuffer;
    }

    public void newFigure(){
        params.refresh();
    }

    public boolean isItAreRealPartOfFigure(int x, int y){
        return (params.getCondition()[y * params.getWidth() + x] == 1);
    }

    public void rotate(){
        params.setPosX(params.getPointerShiftToNextPos()[0] + params.getPosX());
        params.setPosY(params.getPointerShiftToNextPos()[1] + params.getPosY());
    }

    public void moveRight(){
        params.setPosX(params.getPosX()+1);
    }

    public void moveLeft(){
        params.setPosX(params.getPosX()-1);
    }

    public void moveDown(){
        params.setPosY(params.getPosY()+1);
    }

}
