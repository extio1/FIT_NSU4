package viewContrComm;

import model.Figure;
import model.figure.FigureDescriptor;
import org.jetbrains.annotations.NotNull;

public class Package {
    private int[] gameField;

    FigureDescriptor fallingFigure;
    FigureDescriptor nextFigure;

    public Package(int[] sizes){
        gameField = new int[sizes[0] * sizes[1]];
    }

    public void setGameField(int[] gf){
        gameField = gf;
    }

    public void setFigure(Figure figure) {
        fallingFigure = figure.getDescriptor();
    }

    public void setNextFigure(Figure figure) {
        nextFigure = figure.getDescriptor();
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
