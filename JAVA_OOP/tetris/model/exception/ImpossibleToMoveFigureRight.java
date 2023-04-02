package model.exception;

public class ImpossibleToMoveFigureRight extends Exception{
    @Override
    public String getMessage(){
        return "Impossible to move figure right.";
    }

}
