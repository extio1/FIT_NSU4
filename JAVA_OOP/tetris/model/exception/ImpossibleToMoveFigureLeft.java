package model.exception;

public class ImpossibleToMoveFigureLeft extends Exception{
    @Override
    public String getMessage(){
        return "Impossible to move figure left.";
    }

}
