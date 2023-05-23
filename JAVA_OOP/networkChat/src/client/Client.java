package client;

public interface Client {
    void send(String message) throws InterruptedException;
    void closeSession() throws InterruptedException;
    void registerNewUser(String name) throws InterruptedException;
    void requestUserList() throws InterruptedException;
    ClientSessionData getSessionData();
}
