package model.exception;

import model.Figure;

public class FigureAddingToFieldException extends Exception{
    private final Figure figure;
    public FigureAddingToFieldException(Figure figure){
        this.figure = figure;
    }

    @Override
    public String getMessage(){
        return "Exception while adding "+figure+" to field.";
    }
}
