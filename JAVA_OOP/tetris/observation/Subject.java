package observation;

import model.exception.UnattachedObserverException;

public interface Subject {
    void attach(Observer obs);
    void detach(Observer obs);
    void signalizeAll();
    Object getInfo(Observer observer) throws UnattachedObserverException;
}
