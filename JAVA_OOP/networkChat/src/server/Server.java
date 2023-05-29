package server;

import exception.UnknownImplementationException;
import protocol.ObjectServer;
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
    private final static int ECHO_PORT = 7;
    private final List<ClientHandler> clientHandlers = new ArrayList<>();
    private ServerConfiguration configuration;
    private final ServerSocket serverSocket;
    private final ServerSocket serverEchoSocket;
    private final ChatContext context = new ChatContext();
    private final LoggerServer logger = new LoggerServer();

    public Server() throws IOException {
        this("resources/server.properties");
    }

    Server(String configPath) throws IOException {
        readConfig(configPath);
        serverSocket = new ServerSocket(configuration.port);
        serverEchoSocket = new ServerSocket(ECHO_PORT);
        this.start();

        logger.makeLog(Level.INFO, "Server launched: "+serverSocket.getLocalSocketAddress()+
                "\n port "+serverSocket.getLocalPort()+"\n echo port "+serverEchoSocket.getLocalPort());
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
    public ServerConfiguration getConfiguration() { return configuration; }

    public int getLastMessagesN(){
        return configuration.lastMessagesN;
    }

    @Override
    public void run() {
        while(!Thread.interrupted()){
            try {
                while(!Thread.interrupted()) {
                    Socket clientSocket = serverSocket.accept();
                    Socket echoClientSocket = serverEchoSocket.accept();
                    echoClientSocket.setSoTimeout(configuration.timeout);

                    try {
                        clientHandlers.add(new ClientHandler(clientSocket, echoClientSocket,
                                this, configuration.implementation));
                    } catch (IOException | UnknownImplementationException e){
                        logger.makeLog(Level.SEVERE, e.getMessage());
                    }
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
            serverSocket.close();
            serverEchoSocket.close();
        } catch (IOException e) {
            logger.makeLog(Level.SEVERE, e.getMessage());
        }
    }

    public record ServerConfiguration(
            int port,
            ImplementationServer implementation,
            int lastMessagesN,
            int timeout
    ){}

    private void readConfig(String path) throws IOException {
        Properties properties = new Properties();

        try(FileInputStream file = new FileInputStream(path)){
            properties.load(file);
            configuration = new ServerConfiguration(
                    Integer.parseInt(properties.getProperty("port")),
                    ImplementationServer.valueOf(properties.getProperty("implementation")),
                    Integer.parseInt(properties.getProperty("history_size")),
                    Integer.parseInt(properties.getProperty("timeout"))
            );
        }
    }
}
