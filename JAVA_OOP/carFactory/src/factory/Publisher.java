package factory;

import gui.Observer;

public interface Publisher {
    void attach(Observer obs);
    void detach(Observer obs);
    void signalizeAll();
    Package getInfo();
}
