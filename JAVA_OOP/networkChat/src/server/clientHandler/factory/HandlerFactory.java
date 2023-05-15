package server.clientHandler.factory;

import server.ClientHandler;
import server.Server;
import server.clientHandler.ClientHandlerSerialize;

import java.io.IOException;
import java.net.Socket;

public class HandlerFactory {
    public ClientHandler doMakeHandler(Implementations impl, Socket socket, Server server, int id) throws IOException {
        switch (impl){
            case XML -> {
                return null;
            }
            case SERIALIZE -> {
                return new ClientHandlerSerialize(socket, server, id);
            }
        }
        return null;
    }
}
