package model.exception;

public class ImpossibleToMoveFigureDown extends Exception{
    @Override
    public String getMessage(){
        return "Impossible to move figure down.";
    }

}
