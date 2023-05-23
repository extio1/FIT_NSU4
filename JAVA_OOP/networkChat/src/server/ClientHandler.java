package server;

import protocol.ObjectServer;
import server.clientHandler.ServerSessionContext;

public interface ClientHandler {
    void send(ObjectServer msg);
    void shutdown();
    ServerSessionContext getContext();
}
