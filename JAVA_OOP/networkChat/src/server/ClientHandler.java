package server;

import protocol.ObjectServer;

public interface ClientHandler {
    void send(ObjectServer msg);
    void shutdown();
}
