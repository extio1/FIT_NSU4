package model;

import model.exception.*;
import model.figure.FigureDescriptor;
import model.figure.FigureParams;

public enum Figure {
    leftAngle   (new FigureParams(5, 0, (byte) 4,
                new byte[][]{{0, 0}, {0, 0}, {0, 0}, {0, 0}},
                new byte[][]{{2, 3}, {3, 2}, {2, 3}, {3, 2}},
                new byte[][]{{1, 0, 1, 0, 1, 1},
                             {1, 1, 1, 1, 0, 0},
                             {1, 1, 0, 1, 0, 1},
                             {0, 0, 1, 1, 1, 1}})),

    rightAngle  (new FigureParams(5, 0, (byte) 4,
                new byte[][]{{0, 0}, {0, 0}, {0, 0}, {0, 0}},
                new byte[][]{{2, 3}, {3, 2}, {2, 3}, {3, 2}},
                new byte[][]{{0, 1, 0, 1, 1, 1},
                             {1, 0, 0, 1, 1, 1},
                             {1, 1, 1, 0, 1, 0},
                             {1, 1, 1, 0, 0, 1}})),

    leftSnake   (new FigureParams(5, 0, (byte) 2,
                new byte[][]{{0, 0}, {0, 0}},
                new byte[][]{{2, 3}, {3, 2}},
                new byte[][]{{1, 0, 1, 1, 0, 1},
                             {0, 1, 1, 1, 1, 0}})),

    rightSnake  (new FigureParams(5, 0, (byte) 2,
                new byte[][]{{0, 0}, {0, 0}},
                new byte[][]{{2, 3}, {3, 2}},
                new byte[][]{{0, 1, 1, 1, 1, 0},
                             {1, 1, 0, 0, 1, 1}})),

    pedestal    (new FigureParams(5, 0, (byte) 4,
                new byte[][]{{0, 0}, {0, 0}, {0, 0}, {0, 0}},
                new byte[][]{{3, 2}, {2, 3}, {3, 2}, {2, 3}},
                new byte[][]{{0, 1, 0, 1, 1, 1},
                             {1, 0, 1, 1, 1, 0},
                             {1, 1, 1, 0, 1, 0},
                             {0, 1, 1, 1, 0, 1} })),

    stick       (new FigureParams(5, 0, (byte) 4,
                new byte[][]{{0, 1}, {0, 0}, {-1, 0}, {1, 0}},
                new byte[][]{{1, 4}, {4, 1}, {1, 4}, {4, 1}},
                new byte[][]{{1, 1, 1, 1},
                             {1, 1, 1, 1},
                             {1, 1, 1, 1},
                             {1, 1, 1, 1} })),

    cube        (new FigureParams(5, 0, (byte) 1,
                new byte[][]{{0, 0}},
                new byte[][]{{2, 2}},
                new byte[][]{{1, 1, 1, 1}}));

    private enum Direction {
        UP,
        LEFT,
        DOWN,
        RIGHT
    }

    private final FigureParams params;
    private final FigureDescriptor descriptor;
    Figure(FigureParams params){
        this.params = params;
        descriptor = new FigureDescriptor();
    }

    public void newFigure(int myNum){
        params.setOrdinal(myNum);
        params.refresh();
    }


    /**
     *
     * @param buffer The size of buffer will be changed if it was less than 6.
     *               <br>Data is placed inside in the following order:
     *               <br>0 - representation in array  : byte[] (!returns the copy of representation array)
     *               <br>1 - figure length            : byte
     *               <br>2 - figure width             : byte
     *               <br>3 - figure pos x             : int
     *               <br>4 - figure pos y             : int
     *               <br>5 - figure ordinal           : int
     */
    public void getRepresentationParams(Object[] buffer) {
        if(buffer.length < 6){
            buffer = new Object[6];
        }
        buffer[0] = params.getCondition().clone();
        buffer[1] = params.getLength();
        buffer[2] = params.getWidth();
        buffer[3] = params.getPosX();
        buffer[4] = params.getPosY();
        buffer[5] = params.getOrdinal();
    }

    public FigureDescriptor getDescriptor(){
        descriptor.setFigureDescriptorBy(this);
        return descriptor;
    }

    public void addFigureToField(GameField gf) throws FigureAddingToFieldException {
        byte[] view = params.getCondition();
        try {
            for (int i = 0; i < params.getLength(); i++) {
                for (int j = 0; j < params.getWidth(); ++j) {
                    if (view[i * params.getWidth() + j] == 1) {
                        gf.assignValueToPosition(params.getOrdinal(), params.getPosX() + j, params.getPosY() + i);
                    }
                }
            }
        } catch (IndexOutOfField | DimensionOutOfField e){
            throw new FigureAddingToFieldException(this);
        }
    }

    public void rotate(GameField gf) throws ImpossibleToRotateFigure {
        int newLength = params.getNewLength();
        int newWidth = params.getNewWidth();

        /*
            ВОПРОС: Как быть с функциями из которых надо вернуть два числа?
                    Сохранять их в свой буфер внутри текущей функции или обращаться у ней два раза
                    с указанием нужного числа?
         */

        byte[] shift = params.getPointerShiftToNextPos();
        byte[] newFigureView = params.getNextCondition();
        int newPosX = params.getPosX() + shift[0];
        int newPosY = params.getPosY() + shift[1];

        // If figure will go out the field after rotate trying to move it on offset cells in other direction
        if(newPosX + newLength > gf.getSizeField()[0]){
            int offset = newPosX + newLength - gf.getSizeField()[0];
            newPosX -= offset;
        } else if(newPosX < 0){
            newPosX = 0;
        }

        try {
            for (int i = 0; i < newLength; ++i) {
                for (int j = 0; j < newWidth; ++j) {
                    if (!gf.isCellFree(newPosX + j, newPosY + i) && newFigureView[i * newWidth + j] == 1) {
                        throw new ImpossibleToRotateFigure();
                    }
                }
            }
        } catch (IndexOutOfField e){
            System.out.println("INDEX OUT OF BOUNDS");
            throw new ImpossibleToRotateFigure();
        }

        params.setPosX(newPosX);
        params.setPosY(newPosY);
        params.setNextCondition();
    }

    public void moveRight(GameField gf) throws ImpossibleToMoveFigureRight{
        int newRightBorder = params.getPosX() + params.getWidth();
        try {
            if (newRightBorder < gf.getSizeField()[0] && isFreeOnDirection(Direction.RIGHT, gf)) {
                params.setPosX(params.getPosX() + 1);
            } else {
                throw new ImpossibleToMoveFigureRight();
            }
        } catch (IndexOutOfField e) {
            throw new ImpossibleToMoveFigureRight();
        }
    }

    public void moveLeft(GameField gf) throws ImpossibleToMoveFigureLeft {
        int newPosX = params.getPosX() - 1;
        try {
            if (newPosX >= 0 && isFreeOnDirection(Direction.LEFT, gf)) {
                params.setPosX(newPosX);
            } else {
                throw new ImpossibleToMoveFigureLeft();
            }
        } catch (IndexOutOfField e){
            System.out.println("NO MOVE LEFT");
            throw new ImpossibleToMoveFigureLeft();
        }
    }

    public void moveDown(GameField gf) throws ImpossibleToMoveFigureDown {
        if(!isFigureFell(gf)) {
            params.setPosY(params.getPosY()+1);
        } else {
            throw new ImpossibleToMoveFigureDown();
        }
    }

    private boolean isFigureFell(GameField gf) {
        int xPos = params.getPosX();
        int yPos = params.getPosY();
        byte[] figureView = params.getCondition();
        int length = params.getLength();
        int width = params.getWidth();


        int ySizeFld = gf.getSizeField()[1];
        //if there's something under the figure or next Y-coordinate more that Y_SIZE_FLD then figure fell
        if(yPos + length >= ySizeFld){
            return true;
        }

        /*
            ВОПРОС: Если я проверил условие заранее и уверен, что исключения не может произойти, то
            можно как сделать так, чтобы не проверять его дальше? (проверка yPos+length < ySizeFld)
         */

        System.out.println(xPos+" - "+yPos);
        try {
            for (int i = 0; i < length; ++i) {
                for (int j = 0; j < width; ++j) {
                    if (figureView[i * width + j] == 1 && !gf.isCellFree(xPos + j, yPos + i + 1)) {
                        return true;
                    }
                }
            }
        } catch (IndexOutOfField e){
            System.out.println(e.getMessage());
        }

        return false;
    }

    private boolean isFreeOnDirection(Direction direction, GameField gf) throws IndexOutOfField {
        int xPos = params.getPosX();
        int yPos = params.getPosY();
        int length = params.getLength();
        int width = params.getWidth();
        byte[] figureView = params.getCondition();

        switch (direction){
            case LEFT -> {
                for(int i = 0; i < length; ++i){
                    for(int j = 0; j < width; ++j){
                        if(!gf.isCellFree(xPos+j-1, yPos+i) && figureView[i*width+j] == 1){
                            return false;
                        }
                    }
                }
            }
            case RIGHT -> {
                for(int i = 0; i < length; ++i){
                    for(int j = 0; j < width; ++j){
                        if(!gf.isCellFree(xPos+j+1, yPos+i) && figureView[i*width+j] == 1){
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

}
