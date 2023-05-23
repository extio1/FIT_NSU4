package client.clientImpls.clientSerialize;

import client.Client;
import client.exception.ConfigurationException;
import client.exception.UnknownImplementationException;
import protocol.ObjectServer;
import protocol.Request;
import protocol.serverObject.VisitorServer;
import protocol.serverObject.serialization.visitor.VisitorServerSerialize;
import protocol.userObject.UserMessageFabric;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientSerialize implements Client {
    private final ServerListener serverListener;
    private final Sender toServerSender;

    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    private final Socket socket;
    private final ClientProperty configuration;

    private final UserMessageFabric messageFabric;
    private final ClientSessionDataSerialize sessionData = new ClientSessionDataSerialize();

    private final BlockingQueue<Request> toSendQueue;

    private final VisitorServer visitorServerMessages;

    @Override
    public void requestUserList() throws InterruptedException {
        toSendQueue.put(messageFabric.makeListUserRequest());
    }

    @Override
    public ClientSessionDataSerialize getSessionData() {
        return sessionData;
    }

    @Override
    public void send(String message) throws InterruptedException {
        toSendQueue.put(messageFabric.makeMessageUserRequest(message));
    }

    @Override
    public void closeSession() throws InterruptedException {
        toSendQueue.put(messageFabric.makeDetachUserRequest(false));
        toServerSender.interrupt();
        serverListener.interrupt();
    }

    @Override
    public void registerNewUser(String user) throws InterruptedException {
        toSendQueue.put(messageFabric.makeRegisterUserRequest(user));
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
        toServerSender = new Sender();

        toSendQueue = new LinkedBlockingQueue<>();

        visitorServerMessages = new VisitorServerSerialize(sessionData);

        serverListener.setName("Server lister: client thread");
        toServerSender.setName("To server sender: client thread");
        serverListener.start();
        toServerSender.start();

        System.out.println("Client launched at "+socket.getInetAddress()+" port "+socket.getPort());
    }

    private class Sender extends Thread{
        @Override
        public void run() {
            while(!Thread.interrupted()){
                try {
                    Request send = toSendQueue.take();
                    sessionData.addWaitingRequest(send);
                    out.writeObject(send);
                } catch (InterruptedException | IOException e) {
                    break;
                }
            }
        }
    }

    private class ServerListener extends Thread {
        @Override
        public void run() {
            while(!Thread.interrupted()){
                try {
                    ObjectServer srvMess = (ObjectServer) in.readObject();
                    srvMess.accept(visitorServerMessages);
                } catch (IOException | ClassNotFoundException e) {
                    break;
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
