package server.clientHandler;

import server.ClientHandler;
import server.Server;

import java.net.Socket;

public class ClientHandlerXML extends ClientHandler {
    private final Socket socket;

    public ClientHandlerXML(Socket socket, Server server, int id){
        this.socket = socket;
    }

    @Override
    public void send(Object msg) {

    }
}
