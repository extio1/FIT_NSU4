package view.observation;

public interface Subject {
    void attach(Observer obs);
    void detach(Observer obs);
    void signalyzeAll();
    Object getInfo();
}
