package server;

import protocol.ObjectServer;
import server.clientHandler.factory.HandlerFactory;
import server.clientHandler.factory.Implementations;
import server.logger.LoggerServer;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

public class Server extends Thread{
    private final List<ClientHandler> clientHandlers = new ArrayList<>();
    private ServerConfiguration configuration;
    private final ServerSocket socket;
    private final ChatContext context = new ChatContext();
    private final LoggerServer logger = new LoggerServer();

    public Server() throws IOException {
        this("resources/server.properties");
    }

    Server(String configPath) throws IOException {
        readConfig(configPath);
        socket = new ServerSocket(configuration.port);
        this.start();

        logger.makeLog(Level.INFO, "Server launched: "+socket.getLocalSocketAddress()+" port "+socket.getLocalPort());
    }

    public void doBroadcast(ObjectServer msg){
        for(ClientHandler ch: clientHandlers) {
            logger.makeLog(Level.INFO, "Broadcast \ntype:"+msg+"\n to <"+ch.getContext().getUserName()+"> \ntime: "+msg.getDate());
            ch.send(msg);
        }
    }

    public void shutdown(){
        this.interrupt();
        closeSocket();
        for(ClientHandler handler : clientHandlers)
            handler.shutdown();
        logger.makeLog(Level.INFO, "Server shutdown");
    }

    public ChatContext getContext(){
        return context;
    }

    public LoggerServer getLogger(){
        return logger;
    }

    public int getLastMessagesN(){
        return configuration.lastMessagesN;
    }

    @Override
    public void run() {
        while(!Thread.interrupted()){
            try {
                HandlerFactory handlerFactory = new HandlerFactory();
                while(!Thread.interrupted()) {
                    Socket clientSocket = socket.accept();
                    clientSocket.setSoTimeout(configuration.timeout);

                    clientHandlers.add(
                            handlerFactory.doMakeHandler(
                            configuration.implementation, clientSocket,
                            this, logger)
                    );
                }
            } catch (IOException e) {
                logger.makeLog(Level.SEVERE, e.getMessage());
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
            logger.makeLog(Level.SEVERE, e.getMessage());
        }
    }

    private record ServerConfiguration(
            int port,
            Implementations implementation,
            int lastMessagesN,
            int timeout
    ){}

    private void readConfig(String path) throws IOException {
        Properties properties = new Properties();

        try(FileInputStream file = new FileInputStream(path)){
            properties.load(file);
            configuration = new ServerConfiguration(
                    Integer.parseInt(properties.getProperty("port")),
                    Implementations.valueOf(properties.getProperty("protocol")),
                    Integer.parseInt(properties.getProperty("history_size")),
                    Integer.parseInt(properties.getProperty("timeout"))
            );
        }
    }
}
