package server;

public abstract class ClientHandler extends Thread {
    protected abstract void send(Object msg);
}
