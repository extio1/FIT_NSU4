package server.clientHandler.factory;

import server.ClientHandler;
import server.Server;
import server.clientHandler.json.ClientHandlerJson;
import server.clientHandler.serialize.ClientHandlerSerialize;
import server.logger.LoggerServer;

import java.io.IOException;
import java.net.Socket;

public class HandlerFactory {
    public ClientHandler doMakeHandler(Implementations impl, Socket socket, Server server, LoggerServer logger) throws IOException {
        switch (impl){
            case JSON -> {
                return new ClientHandlerJson(socket, server, logger);
            }
            case SERIALIZE -> {
                return new ClientHandlerSerialize(socket, server, logger);
            }
        }
        return null;
    }
}
