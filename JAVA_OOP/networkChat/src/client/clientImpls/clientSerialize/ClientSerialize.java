package client.clientImpls.clientSerialize;

import client.Client;
import client.ClientSessionData;
import client.exception.ConfigurationException;
import client.exception.UnknownImplementationException;
import protocol.ObjectServer;
import protocol.ObjectUser;
import protocol.userObject.UserMessageFabric;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientSerialize implements Client {
    private final ServerListener serverListener;

    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    private final Socket socket;
    private final ClientProperty configuration;

    private final UserMessageFabric messageFabric;
    private final MessageHandler messageHandler;
    private final ClientSessionData sessionData;

    private final BlockingQueue<ObjectUser> toSendQueue;

    @Override
    public void requestUserList() throws InterruptedException {
        toSendQueue.put((ObjectUser) messageFabric.makeListUser());
    }

    @Override
    public void send(String message) throws InterruptedException {
        toSendQueue.put((ObjectUser) messageFabric.makeMessageUser(message));
    }

    @Override
    public void closeSession() throws InterruptedException {
        toSendQueue.put((ObjectUser) messageFabric.makeDetachUser());
    }

    @Override
    public void registerNewUser(String user) throws InterruptedException {
        toSendQueue.put((ObjectUser) messageFabric.makeRegisterUser(user));
    }

    public ClientSerialize() throws IOException, ConfigurationException {
        this("resources/client.properties");
    }

    public ClientSerialize(String configPath) throws IOException, ConfigurationException {
        configuration = setupByConfig(configPath);
        socket = new Socket(configuration.hostName, configuration.port);

        try {
            messageFabric = (UserMessageFabric) Class.forName(configuration.implName).getConstructor().newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException
                 | InstantiationException | IllegalAccessException e){
            throw new UnknownImplementationException(configuration.implName);
        }

        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());

        serverListener = new ServerListener();

        messageHandler = new MessageHandler();
        sessionData = new ClientSessionData();
        toSendQueue = new LinkedBlockingQueue<>();

        serverListener.start();

        System.out.println("Client launched at "+socket.getInetAddress()+" port "+socket.getPort());
    }

    private class Sender extends Thread{
        @Override
        public void run() {
            while(!Thread.interrupted()){
                try {
                    ObjectUser send = toSendQueue.take();
                    sessionData.addWaitingRequest(send);
                    out.writeObject(send);
                } catch (IOException | InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    private class ServerListener extends Thread {
        @Override
        public void run() {
            while(!Thread.interrupted()){
                try {
                    messageHandler.handleServerObject((ObjectServer) in.readObject(), sessionData);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    private record ClientProperty(
            String hostName,
            int port,
            String implName
    ) {}

    private ClientProperty setupByConfig(String path) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(path));

        String ip = properties.getProperty("serverName");
        int port = Integer.parseInt(properties.getProperty("port"));
        String implName = properties.getProperty("implementation");

        return new ClientProperty(ip, port, implName);
    }
}
