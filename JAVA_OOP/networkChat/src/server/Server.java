package server;

import protocol.ObjectServer;
import server.clientHandler.factory.HandlerFactory;
import server.clientHandler.factory.Implementations;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Server extends Thread{
    private final List<ClientHandler> clientHandlers = new ArrayList<>();
    private final HandlerFactory handlerFactory = new HandlerFactory();

    private ServerConfiguration configuration;
    private final ServerSocket socket;

    public Server() throws IOException {
        this("resources/server.properties");
    }

    Server(String configPath) throws IOException {
        readConfig(configPath);
        socket = new ServerSocket(configuration.port);
        this.start();

        System.out.println("Server launched: "+socket.getLocalSocketAddress()+" port "+socket.getLocalPort());
    }

    public void doBroadcast(ObjectServer msg){
        for(ClientHandler ch: clientHandlers) {
            ch.send(msg);
        }
    }

    public void shutdown(){
        this.interrupt();
        closeSocket();
        for(ClientHandler handler : clientHandlers)
            handler.shutdown();
    }

    @Override
    public void run() {
        while(!Thread.interrupted()){
            try {
                while(!Thread.interrupted()) {
                    Socket clientSocket = socket.accept();
                    clientHandlers.add(handlerFactory.doMakeHandler(
                            configuration.implementation, clientSocket,
                            this, clientHandlers.size()));
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            } finally {
                closeSocket();
            }
        }
        closeSocket();
    }

    private void closeSocket(){
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private record ServerConfiguration(
            int port,
            Implementations implementation
    ){}

    private void readConfig(String path) throws IOException {
        Properties properties = new Properties();

        try(FileInputStream file = new FileInputStream(path)){
            properties.load(file);
            configuration = new ServerConfiguration(
                    Integer.parseInt(properties.getProperty("port")),
                    Implementations.valueOf(properties.getProperty("protocol"))
            );
        }
    }
}
