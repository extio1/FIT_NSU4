package model.exception;

import observation.Observer;

public class UnattachedObserverException extends Exception{
    private final String name;

    public UnattachedObserverException(Observer obs){
        name = ""+obs.getClass();
    }
    @Override
    public String getMessage(){
        return "Trying of getting package to unattached observer "+name+". ";
    }
}
