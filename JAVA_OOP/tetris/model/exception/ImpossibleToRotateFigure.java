package model.exception;

public class ImpossibleToRotateFigure extends Exception{
    @Override
    public String getMessage(){
        return "Impossible to rotate figure.";
    }

}
